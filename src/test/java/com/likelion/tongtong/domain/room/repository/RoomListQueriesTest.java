package com.likelion.tongtong.domain.room.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.order.repository.OrderRepository.RoomParticipantCount;
import com.likelion.tongtong.domain.room.dto.RoomListResponse;
import com.likelion.tongtong.domain.room.dto.RoomSummaryResponse;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.spot.entity.Spot;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Store;
import com.likelion.tongtong.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoomListQueriesTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 25, 12, 0);

    private final EntityManager entityManager;
    private final RoomRepository roomRepository;
    private final OrderRepository orderRepository;
    private final JsonMapper jsonMapper;

    private Store store;
    private Spot spot;
    private Menu sharedMenu;

    @Autowired
    RoomListQueriesTest(EntityManager entityManager, RoomRepository roomRepository, OrderRepository orderRepository,
                        JsonMapper jsonMapper) {
        this.entityManager = entityManager;
        this.roomRepository = roomRepository;
        this.orderRepository = orderRepository;
        this.jsonMapper = jsonMapper;
    }

    @BeforeEach
    void setUp() {
        store = Store.builder()
                .name("테스트 가게")
                .category("한식")
                .rating(new BigDecimal("4.5"))
                .reviewCount(0)
                .orderCount(0)
                .minOrderAmount(10_000)
                .deliveryFee(2_000)
                .deliveryMinutes(25)
                .distanceMeters(300)
                .lastOrderTime(LocalTime.of(21, 0))
                .build();
        spot = Spot.builder().name("테스트 스팟").build();
        sharedMenu = Menu.builder().store(store).name("함께 나눌 메뉴").price(12_000).portionable(true).build();
        entityManager.persist(store);
        entityManager.persist(spot);
        entityManager.persist(sharedMenu);
    }

    @Test
    @DisplayName("트랙 필터가 없으면 마감 전인 모집 중 방을 두 트랙에서 찾아 마감순으로 반환한다")
    void findsRecruitingRoomsBeforeDeadlineInOrder() {
        // given
        Room later = saveRoom(Track.PORTION, RoomStatus.RECRUITING, NOW.plusMinutes(10));
        Room sooner = saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, NOW.plusSeconds(1));
        saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, NOW);
        saveRoom(Track.PORTION, RoomStatus.RECRUITING, NOW.minusSeconds(1));
        saveRoom(Track.PORTION, RoomStatus.RECRUIT_COMPLETED, NOW.plusMinutes(1));

        // when
        List<Room> rooms = roomRepository.findRecruitingRooms(RoomStatus.RECRUITING, NOW, null);

        // then
        assertThat(rooms).extracting(Room::getId).containsExactly(sooner.getId(), later.getId());
        assertThat(RoomSummaryResponse.from(rooms.getFirst(), 0).representativeMenuName()).isNull();
    }

    @Test
    @DisplayName("선택한 트랙만 반환하고 일치하는 방이 없으면 빈 목록을 반환한다")
    void filtersByTrackAndReturnsEmptyListWithoutMatches() {
        // given
        Room portionRoom = saveRoom(Track.PORTION, RoomStatus.RECRUITING, NOW.plusMinutes(10));

        // when
        List<Room> portionRooms = roomRepository.findRecruitingRooms(RoomStatus.RECRUITING, NOW, Track.PORTION);
        List<Room> individualRooms = roomRepository.findRecruitingRooms(RoomStatus.RECRUITING, NOW, Track.INDIVIDUAL);

        // then
        assertThat(portionRooms).extracting(Room::getId).containsExactly(portionRoom.getId());
        assertThat(individualRooms).isEmpty();
        assertThat(RoomSummaryResponse.from(portionRooms.getFirst(), 0).representativeMenuName())
                .isEqualTo(sharedMenu.getName());
    }

    @Test
    @DisplayName("지정된 방의 JOINED 주문만 방별로 세고 취소·수령 주문은 제외한다")
    void countsOnlyJoinedOrdersForRequestedRooms() {
        // given
        Room first = saveRoom(Track.PORTION, RoomStatus.RECRUITING, NOW.plusMinutes(10));
        Room second = saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, NOW.plusMinutes(10));
        Room unrequested = saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, NOW.plusMinutes(10));
        saveOrder(first, OrderStatus.JOINED);
        saveOrder(first, OrderStatus.JOINED);
        saveOrder(first, OrderStatus.CANCELED);
        saveOrder(first, OrderStatus.PICKED_UP);
        saveOrder(second, OrderStatus.CANCELED);
        saveOrder(unrequested, OrderStatus.JOINED);

        // when
        List<RoomParticipantCount> counts = orderRepository.countByRoomIdsAndStatus(
                List.of(first.getId(), second.getId()), OrderStatus.JOINED);

        // then
        assertThat(counts).extracting(RoomParticipantCount::getRoomId, RoomParticipantCount::getParticipantCount)
                .containsExactly(tuple(first.getId(), 2L));
    }

    @Test
    @DisplayName("아껴통 대표 메뉴는 JSON null이고 나눠통은 방장 주문이 취소돼도 공유 메뉴 이름을 반환한다")
    void serializesRepresentativeMenuByTrackAfterHostCancellation() {
        // given
        saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, NOW.plusMinutes(10));
        Room portionRoom = saveRoom(Track.PORTION, RoomStatus.RECRUITING, NOW.plusMinutes(20));
        saveOrder(portionRoom, OrderStatus.CANCELED);
        entityManager.flush();
        entityManager.clear();

        // when
        List<RoomSummaryResponse> summaries = roomRepository.findRecruitingRooms(RoomStatus.RECRUITING, NOW, null)
                .stream()
                .map(room -> RoomSummaryResponse.from(room, 0))
                .toList();
        JsonNode rooms = jsonMapper.readTree(jsonMapper.writeValueAsString(new RoomListResponse(summaries))).path("rooms");

        // then
        assertThat(rooms.get(0).path("representativeMenuName").isNull()).isTrue();
        assertThat(rooms.get(1).path("representativeMenuName").asString()).isEqualTo(sharedMenu.getName());
    }

    private Room saveRoom(Track track, RoomStatus status, LocalDateTime deadline) {
        Room room = Room.builder()
                .store(store)
                .spot(spot)
                .track(track)
                .sharedMenu(track == Track.PORTION ? sharedMenu : null)
                .maxParticipants(4)
                .recruitDeadline(deadline)
                .status(status)
                .build();
        entityManager.persist(room);
        return room;
    }

    private void saveOrder(Room room, OrderStatus status) {
        User user = User.create();
        entityManager.persist(user);
        entityManager.persist(Order.builder()
                .room(room)
                .user(user)
                .status(status)
                .paymentMethod(PaymentMethod.CARD)
                .foodAmount(5_000)
                .deliveryFeeShare(500)
                .portionFee(0)
                .totalAmount(5_500)
                .build());
    }
}
