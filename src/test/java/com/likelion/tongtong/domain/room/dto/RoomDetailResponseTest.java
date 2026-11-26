package com.likelion.tongtong.domain.room.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.likelion.tongtong.domain.order.service.OrderAmountCalculator;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.spot.entity.Spot;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Store;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class RoomDetailResponseTest {

    private static final LocalDateTime DEADLINE = LocalDateTime.of(2026, 11, 25, 16, 30);
    private final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();
    private final OrderAmountCalculator calculator = new OrderAmountCalculator();

    @Test
    @DisplayName("아껴통 응답은 공유 메뉴를 JSON null로 포함하고 저장된 상태와 마감 시각을 표시한다")
    void serializesIndividualRoomWithExplicitNull() {
        // given
        Room room = createRoom(Track.INDIVIDUAL, RoomStatus.ARRIVED, null);
        var shares = calculator.calculateShares(room.getTrack(), room.getMaxParticipants(), 2_003, null);

        // when
        JsonNode json = responseJson(RoomDetailResponse.from(room, 2, 15_000, shares, true));

        // then
        assertThat(json).isEqualTo(mapper.readTree("""
                {
                  "code": "SUCCESS",
                  "message": "요청에 성공했습니다.",
                  "data": {
                    "roomId": 15,
                    "storeId": 2,
                    "storeName": "테스트 가게",
                    "track": "INDIVIDUAL",
                    "status": "ARRIVED",
                    "spotName": "정문",
                    "currentParticipants": 2,
                    "maxParticipants": 4,
                    "minOrderAmount": 13500,
                    "currentFoodTotal": 15000,
                    "deliveryFeeShare": 500,
                    "portionFee": 0,
                    "recruitDeadline": "2026-11-25T16:30:00",
                    "sharedMenu": null,
                    "joinedByMe": true
                  }
                }
                """));
    }

    @Test
    @DisplayName("나눠통 응답은 공유 메뉴 원가와 내림한 1인 몫을 각각 표시한다")
    void serializesPortionRoomWithCalculatedShare() {
        // given
        Menu menu = Menu.builder().name("까만찜닭").price(27_803).portionable(true).build();
        setField(menu, "id", 11L);
        Room room = createRoom(Track.PORTION, RoomStatus.RECRUITING, menu);
        var shares = calculator.calculateShares(room.getTrack(), room.getMaxParticipants(), 2_003, menu.getPrice());

        // when
        JsonNode data = responseJson(RoomDetailResponse.from(room, 3, 23_350, shares, false)).path("data");

        // then
        assertThat(data.path("sharedMenu")).isEqualTo(mapper.readTree("""
                {"menuId": 11, "name": "까만찜닭", "price": 27803, "portionPrice": 6950}
                """));
        assertThat(data.path("currentParticipants").intValue()).isEqualTo(3);
        assertThat(data.path("currentFoodTotal").intValue()).isEqualTo(23_350);
        assertThat(data.path("deliveryFeeShare").intValue()).isEqualTo(500);
        assertThat(data.path("portionFee").intValue()).isEqualTo(500);
        assertThat(data.path("joinedByMe").booleanValue()).isFalse();
    }

    private JsonNode responseJson(RoomDetailResponse response) {
        return mapper.readTree(mapper.writeValueAsString(ApiResponse.success(SuccessCode.SUCCESS, response)));
    }

    private Room createRoom(Track track, RoomStatus status, Menu sharedMenu) {
        Store store = Store.builder().name("테스트 가게").minOrderAmount(13_500).build();
        setField(store, "id", 2L);
        Room room = Room.builder()
                .store(store)
                .spot(Spot.builder().name("정문").build())
                .track(track)
                .status(status)
                .sharedMenu(sharedMenu)
                .maxParticipants(4)
                .recruitDeadline(DEADLINE)
                .build();
        setField(room, "id", 15L);
        return room;
    }
}
