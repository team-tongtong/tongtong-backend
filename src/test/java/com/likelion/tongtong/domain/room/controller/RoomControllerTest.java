package com.likelion.tongtong.domain.room.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.spot.entity.Spot;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Store;
import com.likelion.tongtong.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@MockitoBean(types = Clock.class)
@Transactional
class RoomControllerTest {

    private static final String ROOMS_URL = "/api/v1/rooms";
    private static final String STORE_IMAGE_URL = "https://example.com/store.png";
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 25, 12, 0);

    private final MockMvc mockMvc;
    private final EntityManager entityManager;
    private final Clock clock;

    private Store store;
    private Spot spot;
    private Menu sharedMenu;

    @Autowired
    RoomControllerTest(MockMvc mockMvc, EntityManager entityManager, Clock clock) {
        this.mockMvc = mockMvc;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(NOW.atZone(SEOUL).toInstant());
        when(clock.getZone()).thenReturn(SEOUL);
        store = Store.builder()
                .name("테스트 가게")
                .category("한식")
                .imageUrl(STORE_IMAGE_URL)
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
    @DisplayName("필터를 생략하면 모집 중인 두 트랙을 마감순으로 반환하고 참여 인원과 대표 메뉴를 표시한다")
    void returnsRecruitingRoomsInDeadlineOrder() throws Exception {
        // given
        Room portionRoom = saveRoom(Track.PORTION, RoomStatus.RECRUITING, NOW.plusMinutes(30));
        saveOrder(portionRoom, OrderStatus.CANCELED);
        saveOrder(portionRoom, OrderStatus.JOINED);
        saveOrder(portionRoom, OrderStatus.PICKED_UP);
        Room individualRoom = saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, NOW.plusSeconds(1));
        saveRoom(Track.PORTION, RoomStatus.RECRUITING, NOW);
        saveRoom(Track.PORTION, RoomStatus.RECRUITING, NOW.minusSeconds(1));
        saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUIT_COMPLETED, NOW.plusMinutes(5));

        // when
        var response = mockMvc.perform(get(ROOMS_URL));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                .andExpect(jsonPath("$.data.rooms.length()").value(2))
                .andExpect(jsonPath("$.data.rooms[0].roomId").value(individualRoom.getId().intValue()))
                .andExpect(jsonPath("$.data.rooms[0].storeName").value(store.getName()))
                .andExpect(jsonPath("$.data.rooms[0].storeImageUrl").value(STORE_IMAGE_URL))
                .andExpect(jsonPath("$.data.rooms[0].spotName").value(spot.getName()))
                .andExpect(jsonPath("$.data.rooms[0].track").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.data.rooms[0].representativeMenuName").hasJsonPath())
                .andExpect(jsonPath("$.data.rooms[0].representativeMenuName").value(nullValue()))
                .andExpect(jsonPath("$.data.rooms[0].currentParticipants").value(0))
                .andExpect(jsonPath("$.data.rooms[0].maxParticipants").value(4))
                .andExpect(jsonPath("$.data.rooms[0].recruitDeadline")
                        .value(individualRoom.getRecruitDeadline().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.data.rooms[1].roomId").value(portionRoom.getId().intValue()))
                .andExpect(jsonPath("$.data.rooms[1].track").value("PORTION"))
                .andExpect(jsonPath("$.data.rooms[1].representativeMenuName").value(sharedMenu.getName()))
                .andExpect(jsonPath("$.data.rooms[1].currentParticipants").value(1));
    }

    @Test
    @DisplayName("트랙을 지정하면 요청한 트랙의 모집방만 반환한다")
    void returnsOnlyRequestedTrack() throws Exception {
        // given
        saveRoom(Track.PORTION, RoomStatus.RECRUITING, NOW.plusMinutes(10));
        saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, NOW.plusMinutes(10));

        // when and then
        mockMvc.perform(get(ROOMS_URL).param("track", "PORTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rooms.length()").value(1))
                .andExpect(jsonPath("$.data.rooms[0].track").value("PORTION"));
        mockMvc.perform(get(ROOMS_URL).param("track", "INDIVIDUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rooms.length()").value(1))
                .andExpect(jsonPath("$.data.rooms[0].track").value("INDIVIDUAL"));
    }

    @Test
    @DisplayName("잘못된 트랙은 공통 INVALID_PARAMETER 응답을 반환한다")
    void returnsInvalidParameterForUnknownTrack() throws Exception {
        // given and when
        var response = mockMvc.perform(get(ROOMS_URL).param("track", "UNKNOWN"));

        // then
        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("조회할 수 있는 모집방이 없으면 빈 배열을 반환한다")
    void returnsEmptyRoomsArray() throws Exception {
        // given and when
        var response = mockMvc.perform(get(ROOMS_URL));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rooms").isArray())
                .andExpect(jsonPath("$.data.rooms").isEmpty());
    }

    @Test
    @DisplayName("Swagger에 모집방 목록과 nullable 대표 메뉴 스키마가 노출된다")
    void documentsRoomListInOpenApi() throws Exception {
        // given and when
        var response = mockMvc.perform(get("/v3/api-docs"));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/rooms'].get.summary").value("모집방 목록 조회"))
                .andExpect(jsonPath("$.components.schemas.RoomSummaryResponse.properties.representativeMenuName.type")
                        .value(hasItems("string", "null")));
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
                .foodAmount(3_000)
                .deliveryFeeShare(500)
                .portionFee(500)
                .totalAmount(4_000)
                .build());
    }
}
