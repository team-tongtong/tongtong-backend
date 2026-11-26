package com.likelion.tongtong.domain.room.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class RoomCreatedResponseTest {

    @Test
    @DisplayName("모집방 생성 응답은 공통 응답과 초 단위 마감 시각을 반환한다")
    void serializesCreatedResponse() {
        // given
        var mapper = JsonMapper.builder().findAndAddModules().build();
        var result = new RoomCreatedResponse(
                12L, 31L, RoomStatus.RECRUITING, LocalDateTime.of(2026, 11, 25, 12, 10, 0, 123_456_789));

        // when
        var response = ApiResponse.success(SuccessCode.CREATED, result);
        var json = mapper.readTree(mapper.writeValueAsString(response));

        // then
        assertThat(json).isEqualTo(mapper.readTree("""
                {
                  "code": "CREATED",
                  "message": "생성되었습니다.",
                  "data": {
                    "roomId": 12,
                    "orderId": 31,
                    "status": "RECRUITING",
                    "recruitDeadline": "2026-11-25T12:10:00"
                  }
                }
                """));
    }
}
