package com.likelion.tongtong.domain.room.service;

import static org.assertj.core.api.Assertions.assertThat;

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
class RoomAutoCancelTest {

    private final RoomAutoCancelService roomAutoCancelService;
    private final RoomRepository roomRepository;
    private final OrderRepository orderRepository;
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
    @DisplayName("마감 시각이 지난 모집중 방을 취소하고 그 방의 주문과 결제도 취소한다")
    void cancelsExpiredRecruitingRoom() {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Room expired = roomRepository.save(createRoom(RoomStatus.RECRUITING, now.minusMinutes(1)));
        Order joined = orderRepository.save(createOrder(expired, OrderStatus.JOINED));
        Order alreadyCanceled = orderRepository.save(createOrder(expired, OrderStatus.CANCELED));

        // when
        boolean canceled = roomAutoCancelService.cancelIfExpired(expired.getId(), now);

        // then
        assertThat(canceled).isTrue();
        assertThat(roomRepository.findById(expired.getId()).orElseThrow().getStatus()).isEqualTo(RoomStatus.CANCELED);
        Order canceledOrder = orderRepository.findById(joined.getId()).orElseThrow();
        assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(canceledOrder.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(orderRepository.findById(alreadyCanceled.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("마감 전 방과 모집이 완료된 방은 취소하지 않는다")
    void keepsRoomsNotExpired() {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Room notExpired = roomRepository.save(createRoom(RoomStatus.RECRUITING, now.plusMinutes(10)));
        Room completed = roomRepository.save(createRoom(RoomStatus.RECRUIT_COMPLETED, now.minusMinutes(1)));
        Order completedOrder = orderRepository.save(createOrder(completed, OrderStatus.JOINED));

        // when
        boolean notExpiredCanceled = roomAutoCancelService.cancelIfExpired(notExpired.getId(), now);
        boolean completedCanceled = roomAutoCancelService.cancelIfExpired(completed.getId(), now);

        // then
        assertThat(notExpiredCanceled).isFalse();
        assertThat(completedCanceled).isFalse();
        assertThat(roomRepository.findById(notExpired.getId()).orElseThrow().getStatus())
                .isEqualTo(RoomStatus.RECRUITING);
        assertThat(roomRepository.findById(completed.getId()).orElseThrow().getStatus())
                .isEqualTo(RoomStatus.RECRUIT_COMPLETED);
        assertThat(orderRepository.findById(completedOrder.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.JOINED);
    }

    @Test
    @DisplayName("스케줄러는 마감이 지난 모집중 방만 골라 취소한다")
    void schedulerCancelsOnlyExpiredRooms() {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        Room expired = roomRepository.save(createRoom(RoomStatus.RECRUITING, now.minusMinutes(1)));
        Room notExpired = roomRepository.save(createRoom(RoomStatus.RECRUITING, now.plusMinutes(10)));
        RoomAutoCancelScheduler scheduler = new RoomAutoCancelScheduler(roomAutoCancelService, clock);

        // when
        scheduler.cancelExpiredRooms();

        // then
        assertThat(roomRepository.findById(expired.getId()).orElseThrow().getStatus())
                .isEqualTo(RoomStatus.CANCELED);
        assertThat(roomRepository.findById(notExpired.getId()).orElseThrow().getStatus())
                .isEqualTo(RoomStatus.RECRUITING);
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

    private Room createRoom(RoomStatus status, LocalDateTime recruitDeadline) {
        return Room.builder()
                .store(store)
                .spot(spot)
                .track(Track.INDIVIDUAL)
                .maxParticipants(4)
                .recruitDeadline(recruitDeadline)
                .status(status)
                .build();
    }

    private Order createOrder(Room room, OrderStatus status) {
        return Order.builder()
                .room(room)
                .user(userRepository.save(User.create()))
                .status(status)
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(status == OrderStatus.CANCELED ? PaymentStatus.CANCELED : PaymentStatus.PENDING)
                .foodAmount(7_000)
                .deliveryFeeShare(500)
                .portionFee(0)
                .totalAmount(7_500)
                .build();
    }
}
