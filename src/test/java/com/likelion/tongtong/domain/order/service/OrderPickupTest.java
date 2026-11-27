package com.likelion.tongtong.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.likelion.tongtong.domain.order.dto.PickupOrderResponse;
import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import com.likelion.tongtong.domain.order.entity.PaymentStatus;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.room.repository.RoomRepository;
import com.likelion.tongtong.domain.spot.entity.Spot;
import com.likelion.tongtong.domain.spot.repository.SpotRepository;
import com.likelion.tongtong.domain.store.entity.Store;
import com.likelion.tongtong.domain.store.repository.StoreRepository;
import com.likelion.tongtong.domain.user.entity.User;
import com.likelion.tongtong.domain.user.repository.UserRepository;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
class OrderPickupTest {

    private static final long FIXED_USER_ID = 1L;
    private static final long OTHER_USER_ID = 9_201L;
    private static final String PICKUP_CODE = "04821";

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final RoomRepository roomRepository;
    private final StoreRepository storeRepository;
    private final SpotRepository spotRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    private Store store;
    private Spot spot;
    private User me;
    private User otherUser;

    @BeforeEach
    void setUp() {
        // 자동 증가 ID와 겹치지 않도록 사용자는 ID를 지정해 넣는다
        insertUser(FIXED_USER_ID);
        insertUser(OTHER_USER_ID);
        me = userRepository.findById(FIXED_USER_ID).orElseThrow();
        otherUser = userRepository.findById(OTHER_USER_ID).orElseThrow();
        store = storeRepository.save(createStore());
        spot = spotRepository.save(Spot.builder().name("한양대 ERICA 행복관 1층").build());
    }

    @Test
    @DisplayName("배달 완료된 주문에 맞는 픽업 코드를 입력하면 수령 완료로 바뀐다")
    void picksUpWithCorrectCode() {
        // given
        Order order = saveOrder(RoomStatus.ARRIVED, me, OrderStatus.JOINED);

        // when
        PickupOrderResponse response = orderService.pickUpOrder(order.getId(), PICKUP_CODE);

        // then
        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PICKED_UP);
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PICKED_UP);
    }

    @Test
    @DisplayName("픽업 코드가 다르면 INVALID_PICKUP_CODE로 거절하고 상태를 바꾸지 않는다")
    void rejectsWrongCode() {
        // given
        Order order = saveOrder(RoomStatus.ARRIVED, me, OrderStatus.JOINED);

        // when & then
        assertError(order.getId(), "99999", ErrorCode.INVALID_PICKUP_CODE);
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.JOINED);
    }

    @Test
    @DisplayName("배달 완료 전이면 NOT_ARRIVED로 거절한다")
    void rejectsBeforeArrived() {
        // given
        Order order = saveOrder(RoomStatus.RECRUIT_COMPLETED, me, OrderStatus.JOINED);

        // when & then
        assertError(order.getId(), PICKUP_CODE, ErrorCode.NOT_ARRIVED);
    }

    @Test
    @DisplayName("이미 수령한 주문이면 ALREADY_PICKED_UP으로 거절한다")
    void rejectsAlreadyPickedUp() {
        // given
        Order order = saveOrder(RoomStatus.ARRIVED, me, OrderStatus.PICKED_UP);

        // when & then
        assertError(order.getId(), PICKUP_CODE, ErrorCode.ALREADY_PICKED_UP);
    }

    @Test
    @DisplayName("다른 사람의 주문이거나 없는 주문이면 ORDER_NOT_FOUND로 거절한다")
    void rejectsOtherUsersOrUnknownOrder() {
        // given
        Order otherOrder = saveOrder(RoomStatus.ARRIVED, otherUser, OrderStatus.JOINED);

        // when & then
        assertError(otherOrder.getId(), PICKUP_CODE, ErrorCode.ORDER_NOT_FOUND);
        assertError(999_999L, PICKUP_CODE, ErrorCode.ORDER_NOT_FOUND);
    }

    private void assertError(Long orderId, String code, ErrorCode errorCode) {
        assertThatThrownBy(() -> orderService.pickUpOrder(orderId, code))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private Order saveOrder(RoomStatus roomStatus, User user, OrderStatus orderStatus) {
        Room room = roomRepository.save(Room.builder()
                .store(store)
                .spot(spot)
                .track(Track.INDIVIDUAL)
                .maxParticipants(2)
                .recruitDeadline(LocalDateTime.now(clock).plusMinutes(30))
                .status(roomStatus)
                .build());
        return orderRepository.save(Order.builder()
                .room(room)
                .user(user)
                .status(orderStatus)
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(PaymentStatus.PAID)
                .foodAmount(7_000)
                .deliveryFeeShare(1_000)
                .portionFee(0)
                .totalAmount(8_000)
                .pickupCode(PICKUP_CODE)
                .build());
    }

    private void insertUser(long userId) {
        jdbcTemplate.update("""
                insert into users (user_id, created_at, updated_at)
                select ?, current_timestamp, current_timestamp
                where not exists (select 1 from users where user_id = ?)
                """, userId, userId);
    }

    private Store createStore() {
        return Store.builder()
                .name("두찜 안산한대앞역점")
                .category("찜닭")
                .rating(new BigDecimal("4.9"))
                .reviewCount(259)
                .orderCount(1_000)
                .minOrderAmount(13_500)
                .deliveryFee(2_000)
                .deliveryMinutes(32)
                .distanceMeters(300)
                .lastOrderTime(LocalTime.of(23, 50))
                .build();
    }
}
