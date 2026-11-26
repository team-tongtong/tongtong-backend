package com.likelion.tongtong.domain.room.controller;

import com.likelion.tongtong.domain.room.dto.CreateRoomRequest;
import com.likelion.tongtong.domain.room.dto.RoomCreatedResponse;
import com.likelion.tongtong.domain.room.dto.RoomDetailResponse;
import com.likelion.tongtong.domain.room.dto.RoomListResponse;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.room.service.RoomService;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "모집방 생성",
            description = "선택한 주문 방식과 모집 조건으로 모집방을 만들고 방장의 주문을 결제 대기 상태로 저장합니다.")
    public ApiResponse<RoomCreatedResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ApiResponse.success(SuccessCode.CREATED, roomService.createRoom(request));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "공동 주문 상세 조회",
            description = "모집방 정보, 참여 인원, 표시용 음식값 합계와 1인 분담금을 반환합니다.")
    public ApiResponse<RoomDetailResponse> getRoomDetail(@PathVariable("roomId") Long roomId) {
        return ApiResponse.success(SuccessCode.SUCCESS, roomService.getRoomDetail(roomId));
    }
}
