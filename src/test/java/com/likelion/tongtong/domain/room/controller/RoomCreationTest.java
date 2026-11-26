package com.likelion.tongtong.domain.room.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.tongtong.domain.order.repository.OrderItemRepository;
import com.likelion.tongtong.domain.spot.entity.Spot;
import com.likelion.tongtong.domain.spot.repository.SpotRepository;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Store;
import com.likelion.tongtong.domain.store.repository.MenuRepository;
import com.likelion.tongtong.domain.store.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:room-create-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@MockitoBean(types = Clock.class)
@MockitoSpyBean(types = OrderItemRepository.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
class RoomCreationTest {

    private static final String ROOMS_URL = "/api/v1/rooms";
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 26, 12, 0);
    private static final long UNKNOWN_ID = Long.MAX_VALUE;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbc;
    private final StoreRepository storeRepository;
    private final SpotRepository spotRepository;
    private final MenuRepository menuRepository;
    private final OrderItemRepository orderItemRepository;
    private final Clock clock;

    private Store store;
    private Spot spot;
    private Menu personalMenu;
    private Menu shareableMenu;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(NOW.atZone(SEOUL).toInstant());
        when(clock.getZone()).thenReturn(SEOUL);
        jdbc.update("""
                insert into users (user_id, created_at, updated_at)
                select 1, current_timestamp, current_timestamp
                where not exists (select 1 from users where user_id = 1)
                """);
        store = saveStore();
        spot = spotRepository.save(Spot.builder().name("통통 스팟").build());
        personalMenu = saveMenu(store, "콜라", 2_500, false);
        shareableMenu = saveMenu(store, "찜닭", 27_800, true);
    }

    @Test
    @DisplayName("아껴통 모집방과 방장의 개인 메뉴 주문을 저장한다")
    void createsIndividualRoom() throws Exception {
        // given
        String body = request(store.getId(), spot.getId(), "INDIVIDUAL", "null", personalItems(personalMenu, 2), 10);

        // when
        Created created = create(body);

        // then
        assertRoom(created, "INDIVIDUAL", null);
        assertOrder(created, 5_000, 666, 0, 5_666);
        assertThat(items(created.orderId())).containsExactly(new SavedItem(personalMenu.getId(), 2, false));
    }

    @ParameterizedTest(name = "개인 메뉴 포함 여부 {0}")
    @MethodSource("portionAmounts")
    @DisplayName("나눠통 공유 메뉴는 1인 몫만 계산하고 수량 1의 공유 항목으로 저장한다")
    void createsPortionRoom(boolean withPersonalMenu, int food, int total) throws Exception {
        // given
        String items = withPersonalMenu ? personalItems(personalMenu, 1) : "[]";
        String body = request(store.getId(), spot.getId(), "PORTION", sharedMenu(shareableMenu), items, 10);

        // when
        Created created = create(body);

        // then
        assertRoom(created, "PORTION", shareableMenu.getId());
        assertOrder(created, food, 666, 500, total);
        assertThat(items(created.orderId()))
                .containsExactlyElementsOf(withPersonalMenu
                        ? List.of(new SavedItem(shareableMenu.getId(), 1, true),
                                new SavedItem(personalMenu.getId(), 1, false))
                        : List.of(new SavedItem(shareableMenu.getId(), 1, true)));
    }

    static Stream<Arguments> portionAmounts() {
        return Stream.of(Arguments.of(false, 9_266, 10_432), Arguments.of(true, 11_766, 12_932));
    }

    @Test
    @DisplayName("트랙별 메뉴 구성, 모집 시간과 수량 검증 실패는 400으로 응답한다")
    void rejectsInvalidRequests() throws Exception {
        // given
        List<String> requests = List.of(
                request(store.getId(), spot.getId(), "INDIVIDUAL", "null", "[]", 10),
                request(store.getId(), spot.getId(), "PORTION", "null", "[]", 10),
                request(store.getId(), spot.getId(), "INDIVIDUAL", "null", personalItems(personalMenu, 0), 10),
                request(store.getId(), spot.getId(), "INDIVIDUAL", "null", personalItems(personalMenu, 1), 15));

        // when and then
        for (String body : requests) {
            assertError(body, 400, "INVALID_INPUT");
        }
        assertError("{", 400, "INVALID_REQUEST_BODY");
    }

    @Test
    @DisplayName("가게, 스팟, 메뉴의 조회·소속·소분 오류를 공통 응답으로 반환한다")
    void returnsDomainErrors() throws Exception {
        // given
        String personalItems = personalItems(personalMenu, 1);
        Menu otherStoreMenu = saveMenu(saveStore(), "다른 가게 메뉴", 3_000, true);

        // when and then
        assertError(request(UNKNOWN_ID, spot.getId(), "INDIVIDUAL", "null", personalItems, 10),
                404, "STORE_NOT_FOUND");
        assertError(request(store.getId(), UNKNOWN_ID, "INDIVIDUAL", "null", personalItems, 10),
                404, "SPOT_NOT_FOUND");
        assertError(request(store.getId(), spot.getId(), "PORTION", sharedMenu(otherStoreMenu), "[]", 10),
                400, "MENU_STORE_MISMATCH");
        assertError(request(store.getId(), spot.getId(), "PORTION", sharedMenu(personalMenu), "[]", 10),
                400, "NOT_PORTIONABLE_MENU");
        assertError(request(store.getId(), spot.getId(), "INDIVIDUAL", "null",
                        "[{\"menuId\":" + UNKNOWN_ID + ",\"quantity\":1}]", 10),
                400, "INVALID_INPUT");
        assertError(request(store.getId(), spot.getId(), "INDIVIDUAL", "null",
                        personalItems(personalMenu, Integer.MAX_VALUE), 10),
                400, "INVALID_INPUT");
    }

    @Test
    @DisplayName("주문 항목 저장에 실패하면 모집방과 주문도 롤백한다")
    void rollsBackAllWritesOnItemSaveFailure() throws Exception {
        // given
        List<Long> countsBefore = creationCounts();
        String body = request(store.getId(), spot.getId(), "PORTION", sharedMenu(shareableMenu), "[]", 10);
        doThrow(new IllegalStateException("주문 항목 저장 실패")).when(orderItemRepository).saveAll(any());

        // when
        assertError(body, 500, "INTERNAL_SERVER_ERROR");

        // then
        assertThat(creationCounts()).isEqualTo(countsBefore);
    }

    private Created create(String body) throws Exception {
        var result = postRoom(body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.data.status").value("RECRUITING"))
                .andExpect(jsonPath("$.data.recruitDeadline").value("2026-11-26T12:10:00"))
                .andReturn();
        var data = JSON.readTree(result.getResponse().getContentAsString()).get("data");
        return new Created(data.get("roomId").asLong(), data.get("orderId").asLong());
    }

    private ResultActions postRoom(String body) throws Exception {
        return mockMvc.perform(post(ROOMS_URL).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private void assertError(String body, int status, String code) throws Exception {
        postRoom(body).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(status))
                .andExpect(jsonPath("$.code").value(code));
    }

    private void assertRoom(Created created, String track, Long sharedMenuId) {
        Map<String, Object> room = jdbc.queryForMap("select * from room where room_id = ?", created.roomId());
        assertThat(room.get("store_id")).isEqualTo(store.getId());
        assertThat(room.get("spot_id")).isEqualTo(spot.getId());
        assertThat(room.get("track")).isEqualTo(track);
        assertThat(room.get("shared_menu_id")).isEqualTo(sharedMenuId);
        assertThat(room.get("max_participants")).isEqualTo(3);
        assertThat(room.get("status")).isEqualTo("RECRUITING");
        assertThat(jdbc.queryForObject("select recruit_deadline from room where room_id = ?",
                LocalDateTime.class, created.roomId())).isEqualTo(NOW.plusMinutes(10));
    }

    private void assertOrder(Created created, int food, int deliveryFee, int portionFee, int total) {
        Map<String, Object> order = jdbc.queryForMap("select * from orders where order_id = ?", created.orderId());
        assertThat(order.get("room_id")).isEqualTo(created.roomId());
        assertThat(order.get("user_id")).isEqualTo(1L);
        assertThat(order.get("status")).isEqualTo("JOINED");
        assertThat(order.get("payment_method")).isEqualTo("KAKAO_PAY");
        assertThat(order.get("payment_status")).isEqualTo("PENDING");
        assertThat(order.get("food_amount")).isEqualTo(food);
        assertThat(order.get("delivery_fee_share")).isEqualTo(deliveryFee);
        assertThat(order.get("portion_fee")).isEqualTo(portionFee);
        assertThat(order.get("total_amount")).isEqualTo(total);
        assertThat(order.get("pickup_code")).isNull();
    }

    private List<SavedItem> items(long orderId) {
        return jdbc.query("select menu_id, quantity, shared from order_item where order_id = ? order by order_item_id",
                (row, index) -> new SavedItem(row.getLong("menu_id"), row.getInt("quantity"), row.getBoolean("shared")),
                orderId);
    }

    private List<Long> creationCounts() {
        return List.of(
                jdbc.queryForObject("select count(*) from room", Long.class),
                jdbc.queryForObject("select count(*) from orders", Long.class),
                jdbc.queryForObject("select count(*) from order_item", Long.class));
    }

    private Store saveStore() {
        return storeRepository.save(Store.builder().name("테스트 가게").category("한식")
                .rating(new BigDecimal("4.5")).reviewCount(0).orderCount(0).minOrderAmount(10_000)
                .deliveryFee(2_000).deliveryMinutes(20).distanceMeters(300).lastOrderTime(LocalTime.of(21, 0))
                .build());
    }

    private Menu saveMenu(Store store, String name, int price, boolean portionable) {
        return menuRepository.save(Menu.builder().store(store).name(name).price(price).portionable(portionable).build());
    }

    private String request(long storeId, long spotId, String track, String sharedMenu, String items, int minutes) {
        return """
                {
                  "storeId": %d, "spotId": %d, "track": "%s", "sharedMenu": %s, "items": %s,
                  "maxParticipants": 3, "recruitMinutes": %d, "paymentMethod": "KAKAO_PAY"
                }
                """.formatted(storeId, spotId, track, sharedMenu, items, minutes);
    }

    private String sharedMenu(Menu menu) {
        return "{\"menuId\":%d}".formatted(menu.getId());
    }

    private String personalItems(Menu menu, int quantity) {
        return "[{\"menuId\":%d,\"quantity\":%d}]".formatted(menu.getId(), quantity);
    }

    private record Created(long roomId, long orderId) {
    }

    private record SavedItem(long menuId, int quantity, boolean shared) {
    }
}
