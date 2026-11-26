package com.likelion.tongtong.domain.room.controller;

import static org.hamcrest.Matchers.nullValue;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
class RoomDetailControllerTest {

    private static final String URL = "/api/v1/rooms/{roomId}";
    private static final LocalDateTime DEADLINE = LocalDateTime.of(2026, 11, 25, 16, 30);
    private static final long CURRENT_USER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;

    private final MockMvc mockMvc;
    private final EntityManager entityManager;

    private Store store;
    private Spot spot;

    @BeforeEach
    void setUp() {
        saveUser(CURRENT_USER_ID);
        saveUser(OTHER_USER_ID);
        store = Store.builder().name("테스트 가게").category("한식").rating(new BigDecimal("4.5"))
                .reviewCount(0).orderCount(0).minOrderAmount(10_001).deliveryFee(2_000)
                .deliveryMinutes(20).distanceMeters(300).lastOrderTime(LocalTime.of(21, 0)).build();
        spot = Spot.builder().name("통통 스팟").build();
        entityManager.persist(store);
        entityManager.persist(spot);
    }

    @Test
    @DisplayName("아껴통은 저장된 음식값과 본인 참여 여부를 반환하고 공유 메뉴는 null로 표시한다")
    void returnsIndividualRoom() throws Exception {
        // given
        Room room = saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, null);
        saveOrder(room, CURRENT_USER_ID, OrderStatus.JOINED, 6_000);
        saveOrder(room, OTHER_USER_ID, OrderStatus.JOINED, 2_500);

        // when and then
        getDetail(room)
                .andExpect(jsonPath("$.data.roomId").value(room.getId()))
                .andExpect(jsonPath("$.data.storeId").value(store.getId()))
                .andExpect(jsonPath("$.data.storeName").value(store.getName()))
                .andExpect(jsonPath("$.data.spotName").value(spot.getName()))
                .andExpect(jsonPath("$.data.track").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.data.currentParticipants").value(2))
                .andExpect(jsonPath("$.data.maxParticipants").value(3))
                .andExpect(jsonPath("$.data.currentFoodTotal").value(8_500))
                .andExpect(jsonPath("$.data.deliveryFeeShare").value(666))
                .andExpect(jsonPath("$.data.portionFee").value(0))
                .andExpect(jsonPath("$.data.sharedMenu").value(nullValue()))
                .andExpect(jsonPath("$.data.joinedByMe").value(true));
    }

    @Test
    @DisplayName("나눠통은 취소 주문을 제외하고 수령 완료 주문과 내림한 1인 몫을 포함한다")
    void returnsPortionRoomUsingNonCanceledOrders() throws Exception {
        // given
        Menu menu = Menu.builder().store(store).name("공유 메뉴").price(10_001).portionable(true).build();
        entityManager.persist(menu);
        Room room = saveRoom(Track.PORTION, RoomStatus.ARRIVED, menu);
        saveOrder(room, CURRENT_USER_ID, OrderStatus.CANCELED, 9_000);
        saveOrder(room, OTHER_USER_ID, OrderStatus.JOINED, 3_333);
        saveOrder(room, 3L, OrderStatus.PICKED_UP, 3_333);
        saveOrder(room, 4L, OrderStatus.PICKED_UP, 3_333);
        saveOrder(saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, null),
                CURRENT_USER_ID, OrderStatus.JOINED, 10_000);

        // when and then
        getDetail(room)
                .andExpect(jsonPath("$.data.track").value("PORTION"))
                .andExpect(jsonPath("$.data.status").value("ARRIVED"))
                .andExpect(jsonPath("$.data.recruitDeadline").value("2026-11-25T16:30:00"))
                .andExpect(jsonPath("$.data.currentParticipants").value(3))
                .andExpect(jsonPath("$.data.currentFoodTotal").value(9_999))
                .andExpect(jsonPath("$.data.minOrderAmount").value(10_001))
                .andExpect(jsonPath("$.data.deliveryFeeShare").value(666))
                .andExpect(jsonPath("$.data.portionFee").value(500))
                .andExpect(jsonPath("$.data.sharedMenu.menuId").value(menu.getId()))
                .andExpect(jsonPath("$.data.sharedMenu.name").value(menu.getName()))
                .andExpect(jsonPath("$.data.sharedMenu.price").value(10_001))
                .andExpect(jsonPath("$.data.sharedMenu.portionPrice").value(3_333))
                .andExpect(jsonPath("$.data.joinedByMe").value(false));
    }

    @Test
    @DisplayName("주문이 없는 방은 인원과 음식값을 0으로 반환한다")
    void returnsRoomWithoutOrders() throws Exception {
        // given
        Room room = saveRoom(Track.INDIVIDUAL, RoomStatus.RECRUITING, null);

        // when and then
        getDetail(room)
                .andExpect(jsonPath("$.data.currentParticipants").value(0))
                .andExpect(jsonPath("$.data.currentFoodTotal").value(0))
                .andExpect(jsonPath("$.data.joinedByMe").value(false));
    }

    @Test
    @DisplayName("잘못된 방 ID 형식은 400, 존재하지 않는 방은 404로 응답한다")
    void returnsCommonErrorResponses() throws Exception {
        // when and then
        mockMvc.perform(get(URL, "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        mockMvc.perform(get(URL, Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROOM_NOT_FOUND"));
    }

    private ResultActions getDetail(Room room) throws Exception {
        entityManager.flush();
        entityManager.clear();
        return mockMvc.perform(get(URL, room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    private Room saveRoom(Track track, RoomStatus status, Menu sharedMenu) {
        Room room = Room.builder().store(store).spot(spot).track(track).status(status)
                .sharedMenu(sharedMenu).maxParticipants(3).recruitDeadline(DEADLINE).build();
        entityManager.persist(room);
        return room;
    }

    private void saveUser(long userId) {
        entityManager.createNativeQuery("""
                insert into users (user_id, created_at, updated_at)
                select :userId, current_timestamp, current_timestamp
                where not exists (select 1 from users where user_id = :userId)
                """).setParameter("userId", userId).executeUpdate();
    }

    private void saveOrder(Room room, long userId, OrderStatus status, int foodAmount) {
        saveUser(userId);
        int portionFee = room.getTrack() == Track.PORTION ? 500 : 0;
        entityManager.persist(Order.builder().room(room)
                .user(entityManager.getReference(User.class, userId))
                .status(status).paymentMethod(PaymentMethod.CARD)
                .foodAmount(foodAmount).deliveryFeeShare(666).portionFee(portionFee)
                .totalAmount(foodAmount + 666 + portionFee).build());
    }
}
