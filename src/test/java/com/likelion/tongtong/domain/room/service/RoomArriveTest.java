package com.likelion.tongtong.domain.room.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import com.likelion.tongtong.domain.order.entity.PaymentStatus;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.room.dto.ArriveRoomResponse;
import com.likelion.tongtong.domain.room.dto.PickupCodeResponse;
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
import java.util.List;
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
class RoomArriveTest {

    private final RoomService roomService;
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
    @DisplayName("모집 완료된 방을 배달 완료로 바꾸고 취소하지 않은 주문의 픽업 코드를 주문 ID 순으로 반환한다")
    void arrivesCompletedRoom() {
        // given
        Room room = roomRepository.save(createRoom(RoomStatus.RECRUIT_COMPLETED));
        Order first = orderRepository.save(createOrder(room, OrderStatus.JOINED, "04821"));
        orderRepository.save(createOrder(room, OrderStatus.CANCELED, null));
        Order second = orderRepository.save(createOrder(room, OrderStatus.JOINED, "71930"));

        // when
        ArriveRoomResponse response = roomService.arriveRoom(room.getId());

        // then
        assertThat(response.roomId()).isEqualTo(room.getId());
        assertThat(response.status()).isEqualTo(RoomStatus.ARRIVED);
        assertThat(response.pickupCodes()).containsExactly(
                new PickupCodeResponse(first.getId(), "04821"),
                new PickupCodeResponse(second.getId(), "71930"));
        assertThat(roomRepository.findById(room.getId()).orElseThrow().getStatus()).isEqualTo(RoomStatus.ARRIVED);
    }

    @Test
    @DisplayName("모집 완료가 아닌 방은 INVALID_STATUS_TRANSITION으로 거절한다")
    void rejectsRoomNotCompleted() {
        // given
        List<Room> rooms = List.of(
                roomRepository.save(createRoom(RoomStatus.RECRUITING)),
                roomRepository.save(createRoom(RoomStatus.ARRIVED)),
                roomRepository.save(createRoom(RoomStatus.CANCELED)));

        // when & then
        for (Room room : rooms) {
            assertError(room.getId(), ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    @Test
    @DisplayName("없는 모집방이면 ROOM_NOT_FOUND로 거절한다")
    void rejectsUnknownRoom() {
        // when & then
        assertError(999_999L, ErrorCode.ROOM_NOT_FOUND);
    }

    private void assertError(Long roomId, ErrorCode errorCode) {
        assertThatThrownBy(() -> roomService.arriveRoom(roomId))
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
                .maxParticipants(2)
                .recruitDeadline(LocalDateTime.now(clock).plusMinutes(30))
                .status(status)
                .build();
    }

    private Order createOrder(Room room, OrderStatus status, String pickupCode) {
        return Order.builder()
                .room(room)
                .user(userRepository.save(User.create()))
                .status(status)
                .paymentMethod(PaymentMethod.CARD)
                .paymentStatus(pickupCode == null ? PaymentStatus.CANCELED : PaymentStatus.PAID)
                .foodAmount(7_000)
                .deliveryFeeShare(1_000)
                .portionFee(0)
                .totalAmount(8_000)
                .pickupCode(pickupCode)
                .build();
    }
}
