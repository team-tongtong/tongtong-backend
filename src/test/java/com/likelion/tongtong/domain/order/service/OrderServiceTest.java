package com.likelion.tongtong.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.likelion.tongtong.domain.order.dto.OrderDetailResponse;
import com.likelion.tongtong.domain.order.dto.OrderItemResponse;
import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderItem;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import com.likelion.tongtong.domain.order.repository.OrderItemRepository;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
class OrderServiceTest {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RoomRepository roomRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final SpotRepository spotRepository;
    private final UserRepository userRepository;

    private Store store;
    private Spot spot;

    @BeforeEach
    void setUp() {
        store = storeRepository.save(createStore());
        spot = spotRepository.save(Spot.builder().name("한양대 ERICA 행복관 1층").build());
    }

    @Test
    @DisplayName("없는 주문 ID로 조회하면 ORDER_NOT_FOUND로 거절한다")
    void rejectsUnknownOrder() {
        // given
        Long unknownOrderId = 999L;

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(unknownOrderId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND));
    }

    @Test
    @DisplayName("현재 인원은 취소한 주문을 빼고 센다")
    void countsParticipantsExceptCanceled() {
        // given
        Room room = roomRepository.save(createRoom(RoomStatus.RECRUITING));
        Order myOrder = orderRepository.save(createOrder(room, OrderStatus.JOINED));
        orderRepository.save(createOrder(room, OrderStatus.JOINED));
        orderRepository.save(createOrder(room, OrderStatus.CANCELED));

        // when
        OrderDetailResponse response = orderService.getOrder(myOrder.getId());

        // then
        assertThat(response.currentParticipants()).isEqualTo(2);
        assertThat(response.maxParticipants()).isEqualTo(4);
    }

    @Test
    @DisplayName("담은 메뉴를 메뉴 이름, 수량, 함께 나눌 메뉴 여부로 반환한다")
    void returnsOrderItems() {
        // given
        Room room = roomRepository.save(createRoom(RoomStatus.RECRUITING));
        Order order = orderRepository.save(createOrder(room, OrderStatus.JOINED));
        Menu sharedMenu = menuRepository.save(createMenu("까만찜닭", true));
        Menu personalMenu = menuRepository.save(createMenu("콜라 1.25L", false));
        orderItemRepository.save(OrderItem.builder().order(order).menu(sharedMenu).quantity(1).shared(true).build());
        orderItemRepository.save(OrderItem.builder().order(order).menu(personalMenu).quantity(2).shared(false).build());

        // when
        OrderDetailResponse response = orderService.getOrder(order.getId());

        // then
        assertThat(response.items()).containsExactly(
                new OrderItemResponse("까만찜닭", 1, true),
                new OrderItemResponse("콜라 1.25L", 2, false));
    }

    @Test
    @DisplayName("모집중인 방에 참여 중이면 취소할 수 있다")
    void cancelableWhenRecruitingAndJoined() {
        // given
        Room room = roomRepository.save(createRoom(RoomStatus.RECRUITING));
        Order order = orderRepository.save(createOrder(room, OrderStatus.JOINED));

        // when
        OrderDetailResponse response = orderService.getOrder(order.getId());

        // then
        assertThat(response.cancelable()).isTrue();
    }

    @Test
    @DisplayName("모집이 완료된 방의 주문은 취소할 수 없다")
    void notCancelableWhenRecruitCompleted() {
        // given
        Room room = roomRepository.save(createRoom(RoomStatus.RECRUIT_COMPLETED));
        Order order = orderRepository.save(createOrder(room, OrderStatus.JOINED));

        // when
        OrderDetailResponse response = orderService.getOrder(order.getId());

        // then
        assertThat(response.cancelable()).isFalse();
    }

    @Test
    @DisplayName("이미 취소한 주문은 다시 취소할 수 없다")
    void notCancelableWhenAlreadyCanceled() {
        // given
        Room room = roomRepository.save(createRoom(RoomStatus.RECRUITING));
        Order order = orderRepository.save(createOrder(room, OrderStatus.CANCELED));

        // when
        OrderDetailResponse response = orderService.getOrder(order.getId());

        // then
        assertThat(response.cancelable()).isFalse();
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

    private Menu createMenu(String name, boolean portionable) {
        return Menu.builder()
                .store(store)
                .name(name)
                .price(10_000)
                .portionable(portionable)
                .build();
    }

    private Room createRoom(RoomStatus status) {
        return Room.builder()
                .store(store)
                .spot(spot)
                .track(Track.INDIVIDUAL)
                .maxParticipants(4)
                .recruitDeadline(LocalDateTime.of(2026, 11, 24, 16, 0))
                .status(status)
                .build();
    }

    private Order createOrder(Room room, OrderStatus status) {
        return Order.builder()
                .room(room)
                .user(userRepository.save(User.create()))
                .status(status)
                .paymentMethod(PaymentMethod.KAKAO_PAY)
                .foodAmount(7_000)
                .deliveryFeeShare(500)
                .portionFee(0)
                .totalAmount(7_500)
                .build();
    }
}
