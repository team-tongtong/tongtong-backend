package com.likelion.tongtong.domain.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.likelion.tongtong.domain.order.service.OrderAmountCalculator.OrderShares;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "공동 주문 상세 조회 응답")
public record RoomDetailResponse(
        Long roomId,
        Long storeId,
        String storeName,
        Track track,
        RoomStatus status,
        String spotName,
        int currentParticipants,
        int maxParticipants,
        int minOrderAmount,
        @Schema(description = "참여 주문의 음식값 합계. 배달비, 소분 수수료 및 내림 잔액 제외")
        int currentFoodTotal,
        int deliveryFeeShare,
        int portionFee,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime recruitDeadline,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @Schema(description = "함께 나눌 메뉴. 아껴통은 null", nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        SharedMenuResponse sharedMenu,
        boolean joinedByMe
) {

    public static RoomDetailResponse from(Room room, int currentParticipants, int currentFoodTotal,
                                          OrderShares shares, boolean joinedByMe) {
        Store store = room.getStore();
        Menu menu = room.getSharedMenu();
        SharedMenuResponse sharedMenu = room.getTrack() == Track.PORTION
                ? new SharedMenuResponse(menu.getId(), menu.getName(), menu.getPrice(), shares.sharedMenuPortionPrice())
                : null;

        return new RoomDetailResponse(
                room.getId(),
                store.getId(),
                store.getName(),
                room.getTrack(),
                room.getStatus(),
                room.getSpot().getName(),
                currentParticipants,
                room.getMaxParticipants(),
                store.getMinOrderAmount(),
                currentFoodTotal,
                shares.deliveryFeeShare(),
                shares.portionFee(),
                room.getRecruitDeadline(),
                sharedMenu,
                joinedByMe
        );
    }

    public record SharedMenuResponse(Long menuId, String name, int price, int portionPrice) {
    }
}
