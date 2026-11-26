package com.likelion.tongtong.domain.room.dto;

import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.Track;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record RoomSummaryResponse(
        Long roomId,
        String storeName,
        String storeImageUrl,
        Track track,
        @Schema(types = {"string", "null"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String representativeMenuName,
        String spotName,
        long currentParticipants,
        int maxParticipants,
        LocalDateTime recruitDeadline
) {

    public static RoomSummaryResponse from(Room room, long currentParticipants) {
        String representativeMenuName = room.getTrack() == Track.PORTION
                ? room.getSharedMenu().getName()
                : null;
        return new RoomSummaryResponse(
                room.getId(),
                room.getStore().getName(),
                room.getStore().getImageUrl(),
                room.getTrack(),
                representativeMenuName,
                room.getSpot().getName(),
                currentParticipants,
                room.getMaxParticipants(),
                room.getRecruitDeadline()
        );
    }
}
