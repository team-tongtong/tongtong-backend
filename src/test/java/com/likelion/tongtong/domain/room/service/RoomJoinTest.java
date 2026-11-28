package com.likelion.tongtong.domain.room.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderItem;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import com.likelion.tongtong.domain.order.entity.PaymentStatus;
import com.likelion.tongtong.domain.order.repository.OrderItemRepository;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.room.dto.JoinRoomRequest;
import com.likelion.tongtong.domain.room.dto.JoinRoomRequest.MenuItem;
import com.likelion.tongtong.domain.room.dto.JoinRoomResponse;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.room.repository.RoomRepository;
import com.likelion.tongtong.domain.spot.entity.Spot;
import com.likelion.tongtong.domain.spot.repository.SpotRepository;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Store;
import com.likelion.tongtong.domain.store.repository.MenuRepository;
import com.likelion.tongtong.domain.store.repository.StoreRepository;
import com.likelion.tongtong.domain.user.entity.User;
import com.likelion.tongtong.domain.user.repository.UserRepository;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
class RoomJoinTest {

    private static final long FIXED_USER_ID = 1L;
    private static final long OTHER_USER_ID = 9_001L;
    private static final long ANOTHER_USER_ID = 9_002L;

    private final RoomService roomService;
    private final RoomRepository roomRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final SpotRepository spotRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    // 서비스와 같은 기준(KST)으로 마감 시각을 만들어야 서버 시간대와 상관없이 통과한다
    private final Clock clock;

    private Store store;
    private Spot spot;
    private Menu burger;
    private Menu jjimdak;
    private User otherUser;

    @BeforeEach
    void setUp() {
        insertUser(FIXED_USER_ID);
        insertUser(OTHER_USER_ID);
        insertUser(ANOTHER_USER_ID);
        otherUser = userRepository.findById(OTHER_USER_ID).orElseThrow();
        store = storeRepository.save(createStore());
        spot = spotRepository.save(Spot.builder().name("한양대 ERICA 행복관 1층").build());
        burger = menuRepository.save(createMenu(store, "불고기 버거 세트", 7_000, false));
        jjimdak = menuRepository.save(createMenu(store, "까만찜닭", 27_800, true));
    }

    @Test
    @DisplayName("자리가 남은 모집방에 참여하면 결제 대기 주문을 저장하고 모집중을 유지한다")
    void joinsRecruitingRoom() {
        // given
        Room room = saveRoom(Track.INDIVIDUAL, null, 3, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));
        saveOrder(room, otherUser, OrderStatus.JOINED, 7_000);

        // when
        JoinRoomResponse response = roomService.joinRoom(room.getId(), request(new MenuItem(burger.getId(), 1)));

        // then
        assertThat(response.roomStatus()).isEqualTo(RoomStatus.RECRUITING);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.foodAmount()).isEqualTo(7_000);
        assertThat(response.deliveryFeeShare()).isEqualTo(666);
        assertThat(response.portionFee()).isZero();
        assertThat(response.totalAmount()).isEqualTo(7_666);
        Order saved = orderRepository.findById(response.orderId()).orElseThrow();
        assertThat(saved.getUser().getId()).isEqualTo(FIXED_USER_ID);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.JOINED);
    }

    @Test
    @DisplayName("이 참여로 정원과 최소주문금액이 채워지면 모집 완료, 전원 결제 완료, 픽업 코드를 발급한다")
    void completesRecruitWhenFilled() {
        // given
        Room room = saveRoom(Track.INDIVIDUAL, null, 2, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));
        Order hostOrder = saveOrder(room, otherUser, OrderStatus.JOINED, 7_000);

        // when
        JoinRoomResponse response = roomService.joinRoom(room.getId(), request(new MenuItem(burger.getId(), 1)));

        // then
        assertThat(response.roomStatus()).isEqualTo(RoomStatus.RECRUIT_COMPLETED);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        Order myOrder = orderRepository.findById(response.orderId()).orElseThrow();
        assertThat(List.of(hostOrder, myOrder))
                .allSatisfy(order -> {
                    assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
                    assertThat(order.getPickupCode()).matches("\\d{5}");
                });
        assertThat(hostOrder.getPickupCode()).isNotEqualTo(myOrder.getPickupCode());
    }

    @Test
    @DisplayName("마지막 자리인데 음식값 합계가 최소주문금액보다 적으면 MIN_ORDER_NOT_MET로 거절하고 주문을 저장하지 않는다")
    void rejectsLastSeatWhenMinOrderAmountNotMet() {
        // given: 1,000원 + 7,000원 = 8,000원 < 최소주문 13,500원
        Room room = saveRoom(Track.INDIVIDUAL, null, 2, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));
        saveOrder(room, otherUser, OrderStatus.JOINED, 1_000);

        // when & then
        assertError(() -> roomService.joinRoom(room.getId(), request(new MenuItem(burger.getId(), 1))),
                ErrorCode.MIN_ORDER_NOT_MET);
        assertThat(orderRepository.countByRoomIdAndStatusNot(room.getId(), OrderStatus.CANCELED)).isEqualTo(1);
        assertThat(roomRepository.findById(room.getId()).orElseThrow().getStatus()).isEqualTo(RoomStatus.RECRUITING);
    }

    @Test
    @DisplayName("마지막 자리가 아니면 음식값 합계가 최소주문금액보다 적어도 참여할 수 있다")
    void allowsNonLastSeatBelowMinOrderAmount() {
        // given: 정원 3명 중 2번째 참여, 1,000원 + 7,000원 = 8,000원 < 최소주문 13,500원
        Room room = saveRoom(Track.INDIVIDUAL, null, 3, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));
        saveOrder(room, otherUser, OrderStatus.JOINED, 1_000);

        // when
        JoinRoomResponse response = roomService.joinRoom(room.getId(), request(new MenuItem(burger.getId(), 1)));

        // then
        assertThat(response.roomStatus()).isEqualTo(RoomStatus.RECRUITING);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("나눠통은 내림 잔액까지 합쳐 최소주문금액을 판단한다")
    void includesPortionRemainderInMinOrderAmount() {
        // given: 10,001원 메뉴를 3명이 나누면 1인 몫 3,333원, 합계 9,999원 + 잔액 2원 = 10,001원
        Store portionStore = storeRepository.save(createStore(10_000));
        Menu sharedMenu = menuRepository.save(createMenu(portionStore, "나눠 먹는 세트", 10_001, true));
        Room room = roomRepository.save(Room.builder()
                .store(portionStore)
                .spot(spot)
                .track(Track.PORTION)
                .sharedMenu(sharedMenu)
                .maxParticipants(3)
                .recruitDeadline(LocalDateTime.now(clock).plusMinutes(30))
                .build());
        saveOrder(room, otherUser, OrderStatus.JOINED, 3_333);
        saveOrder(room, userRepository.findById(ANOTHER_USER_ID).orElseThrow(), OrderStatus.JOINED, 3_333);

        // when
        JoinRoomResponse response = roomService.joinRoom(room.getId(), request());

        // then
        assertThat(response.foodAmount()).isEqualTo(3_333);
        assertThat(response.roomStatus()).isEqualTo(RoomStatus.RECRUIT_COMPLETED);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("나눠통은 함께 나눌 메뉴 1몫을 자동으로 담고 소분 수수료를 더한다")
    void addsSharedMenuPortionForPortionTrack() {
        // given
        Room room = saveRoom(Track.PORTION, jjimdak, 4, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));
        saveOrder(room, otherUser, OrderStatus.JOINED, 6_950);

        // when
        JoinRoomResponse response = roomService.joinRoom(room.getId(), request());

        // then
        assertThat(response.foodAmount()).isEqualTo(6_950);
        assertThat(response.deliveryFeeShare()).isEqualTo(500);
        assertThat(response.portionFee()).isEqualTo(500);
        assertThat(response.totalAmount()).isEqualTo(7_950);
        List<OrderItem> items = orderItemRepository.findAll().stream()
                .filter(item -> item.getOrder().getId().equals(response.orderId()))
                .toList();
        assertThat(items).singleElement().satisfies(item -> {
            assertThat(item.getMenu().getId()).isEqualTo(jjimdak.getId());
            assertThat(item.getQuantity()).isEqualTo(1);
            assertThat(item.isShared()).isTrue();
        });
    }

    @Test
    @DisplayName("이미 취소하지 않은 주문이 있으면 ALREADY_JOINED로 거절한다")
    void rejectsAlreadyJoined() {
        // given
        Room room = saveRoom(Track.INDIVIDUAL, null, 4, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));
        saveOrder(room, userRepository.findById(FIXED_USER_ID).orElseThrow(), OrderStatus.JOINED, 7_000);

        // when & then
        assertError(() -> roomService.joinRoom(room.getId(), request(new MenuItem(burger.getId(), 1))),
                ErrorCode.ALREADY_JOINED);
    }

    @Test
    @DisplayName("취소한 뒤에는 같은 모집방에 다시 참여할 수 있다")
    void allowsRejoinAfterCancel() {
        // given
        Room room = saveRoom(Track.INDIVIDUAL, null, 4, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));
        saveOrder(room, userRepository.findById(FIXED_USER_ID).orElseThrow(), OrderStatus.CANCELED, 7_000);

        // when
        JoinRoomResponse response = roomService.joinRoom(room.getId(), request(new MenuItem(burger.getId(), 1)));

        // then
        assertThat(response.orderId()).isNotNull();
    }

    @Test
    @DisplayName("마감 시각이 지났거나 모집중이 아니거나 자리가 없으면 RECRUIT_CLOSED로 거절한다")
    void rejectsClosedRooms() {
        // given
        Room expired = saveRoom(Track.INDIVIDUAL, null, 4, RoomStatus.RECRUITING, LocalDateTime.now(clock).minusMinutes(1));
        Room completed = saveRoom(Track.INDIVIDUAL, null, 4, RoomStatus.RECRUIT_COMPLETED,
                LocalDateTime.now(clock).plusMinutes(30));
        Room full = saveRoom(Track.INDIVIDUAL, null, 2, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));
        saveOrder(full, otherUser, OrderStatus.JOINED, 1_000);
        saveOrder(full, userRepository.findById(ANOTHER_USER_ID).orElseThrow(), OrderStatus.JOINED, 1_000);
        JoinRoomRequest request = request(new MenuItem(burger.getId(), 1));

        // when & then
        for (Room room : List.of(expired, completed, full)) {
            assertError(() -> roomService.joinRoom(room.getId(), request), ErrorCode.RECRUIT_CLOSED);
        }
    }

    @Test
    @DisplayName("없는 모집방이면 ROOM_NOT_FOUND로 거절한다")
    void rejectsUnknownRoom() {
        // when & then
        assertError(() -> roomService.joinRoom(999_999L, request(new MenuItem(burger.getId(), 1))),
                ErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("아껴통에 메뉴 없이 참여하면 INVALID_INPUT과 안내 문구로 거절한다")
    void rejectsIndividualWithoutMenu() {
        // given
        Room room = saveRoom(Track.INDIVIDUAL, null, 4, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));

        // when & then
        assertThatThrownBy(() -> roomService.joinRoom(room.getId(), request()))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
                    assertThat(exception.getMessage()).isEqualTo("아껴통은 메뉴를 1개 이상 담아 주세요.");
                });
    }

    @Test
    @DisplayName("다른 가게 메뉴는 MENU_STORE_MISMATCH, 없는 메뉴는 MENU_NOT_FOUND로 거절한다")
    void rejectsInvalidMenus() {
        // given
        Room room = saveRoom(Track.INDIVIDUAL, null, 4, RoomStatus.RECRUITING, LocalDateTime.now(clock).plusMinutes(30));
        Store otherStore = storeRepository.save(createStore());
        Menu otherStoreMenu = menuRepository.save(createMenu(otherStore, "다른 가게 메뉴", 5_000, false));

        // when & then
        assertError(() -> roomService.joinRoom(room.getId(), request(new MenuItem(otherStoreMenu.getId(), 1))),
                ErrorCode.MENU_STORE_MISMATCH);
        assertError(() -> roomService.joinRoom(room.getId(), request(new MenuItem(999_999L, 1))),
                ErrorCode.MENU_NOT_FOUND);
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private JoinRoomRequest request(MenuItem... items) {
        return new JoinRoomRequest(List.of(items), PaymentMethod.TOSS_PAY);
    }

    private void insertUser(long userId) {
        jdbcTemplate.update("""
                insert into users (user_id, created_at, updated_at)
                select ?, current_timestamp, current_timestamp
                where not exists (select 1 from users where user_id = ?)
                """, userId, userId);
    }

    private Store createStore() {
        return createStore(13_500);
    }

    private Store createStore(int minOrderAmount) {
        return Store.builder()
                .name("두찜 안산한대앞역점")
                .category("찜닭")
                .rating(new BigDecimal("4.9"))
                .reviewCount(259)
                .orderCount(1_000)
                .minOrderAmount(minOrderAmount)
                .deliveryFee(2_000)
                .deliveryMinutes(32)
                .distanceMeters(300)
                .lastOrderTime(LocalTime.of(23, 50))
                .build();
    }

    private Menu createMenu(Store menuStore, String name, int price, boolean portionable) {
        return Menu.builder()
                .store(menuStore)
                .name(name)
                .price(price)
                .portionable(portionable)
                .build();
    }

    private Room saveRoom(Track track, Menu sharedMenu, int maxParticipants, RoomStatus status,
                          LocalDateTime recruitDeadline) {
        return roomRepository.save(Room.builder()
                .store(store)
                .spot(spot)
                .track(track)
                .sharedMenu(sharedMenu)
                .maxParticipants(maxParticipants)
                .recruitDeadline(recruitDeadline)
                .status(status)
                .build());
    }

    private Order saveOrder(Room room, User user, OrderStatus status, int foodAmount) {
        return orderRepository.save(Order.builder()
                .room(room)
                .user(user)
                .status(status)
                .paymentMethod(PaymentMethod.CARD)
                .foodAmount(foodAmount)
                .deliveryFeeShare(500)
                .portionFee(0)
                .totalAmount(foodAmount + 500)
                .build());
    }
}
