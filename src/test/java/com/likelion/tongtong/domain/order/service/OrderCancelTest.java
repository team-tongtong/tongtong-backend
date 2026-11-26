package com.likelion.tongtong.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.likelion.tongtong.domain.order.dto.CancelOrderResponse;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
class OrderCancelTest {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final RoomRepository roomRepository;
    private final StoreRepository storeRepository;
    private final SpotRepository spotRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    private Store store;
    private Spot spot;

    @BeforeEach
    void setUp() {
        store = storeRepository.save(createStore());
        spot = spotRepository.save(Spot.builder().name("한양대 ERICA 행복관 1층").build());
    }

    @Test
    @DisplayName("모집중인 방에 참여 중인 주문을 취소하면 주문과 결제 상태가 취소되고 모집방은 유지된다")
    void cancelsJoinedOrderInRecruitingRoom() {
        // given
        Room room = roomRepository.save(createRoom(RoomStatus.RECRUITING));
        Order hostOrder = orderRepository.save(createOrder(room, OrderStatus.JOINED));
        orderRepository.save(createOrder(room, OrderStatus.JOINED));

        // when
        CancelOrderResponse response = orderService.cancelOrder(hostOrder.getId());

        // then
        assertThat(response.orderId()).isEqualTo(hostOrder.getId());
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.CANCELED);
        Order canceled = orderRepository.findById(hostOrder.getId()).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(canceled.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(roomRepository.findById(room.getId()).orElseThrow().getStatus()).isEqualTo(RoomStatus.RECRUITING);
        assertThat(orderRepository.countByRoomIdAndStatusNot(room.getId(), OrderStatus.CANCELED)).isEqualTo(1);
    }

    @Test
    @DisplayName("모집이 완료된 방의 주문은 CANNOT_CANCEL로 거절한다")
    void rejectsCancelAfterRecruitCompleted() {
        // given
        Room room = roomRepository.save(createRoom(RoomStatus.RECRUIT_COMPLETED));
        Order order = orderRepository.save(createOrder(room, OrderStatus.JOINED));

        // when & then
        assertError(order.getId(), ErrorCode.CANNOT_CANCEL);
    }

    @Test
    @DisplayName("이미 취소한 주문은 CANNOT_CANCEL로 거절한다")
    void rejectsAlreadyCanceledOrder() {
        // given
        Room room = roomRepository.save(createRoom(RoomStatus.RECRUITING));
        Order order = orderRepository.save(createOrder(room, OrderStatus.CANCELED));

        // when & then
        assertError(order.getId(), ErrorCode.CANNOT_CANCEL);
    }

    @Test
    @DisplayName("없는 주문이면 ORDER_NOT_FOUND로 거절한다")
    void rejectsUnknownOrder() {
        // when & then
        assertError(999_999L, ErrorCode.ORDER_NOT_FOUND);
    }

    private void assertError(Long orderId, ErrorCode errorCode) {
        assertThatThrownBy(() -> orderService.cancelOrder(orderId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
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

    private Room createRoom(RoomStatus status) {
        return Room.builder()
                .store(store)
                .spot(spot)
                .track(Track.INDIVIDUAL)
                .maxParticipants(4)
                .recruitDeadline(LocalDateTime.now(clock).plusMinutes(30))
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
