package com.likelion.tongtong.domain.room.controller;

import com.likelion.tongtong.domain.room.dto.RoomListResponse;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.room.service.RoomService;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
@Tag(name = "모집방", description = "모집방 API")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "모집방 목록 조회",
            description = "모집 중이며 마감 전인 방을 마감 시각순으로 반환합니다. 트랙을 생략하면 두 트랙을 모두 조회합니다.")
    public ApiResponse<RoomListResponse> getRooms(@RequestParam(name = "track", required = false) Track track) {
        return ApiResponse.success(SuccessCode.SUCCESS, roomService.getRooms(track));
    }
}
