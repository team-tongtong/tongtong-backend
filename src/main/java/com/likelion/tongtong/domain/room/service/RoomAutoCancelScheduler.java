package com.likelion.tongtong.domain.room.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class RoomAutoCancelScheduler {

    private final RoomAutoCancelService roomAutoCancelService;
    private final Clock clock;

    // 마감 시각까지 인원이나 최소주문금액을 채우지 못한 모집방을 1분마다 취소한다
    @Scheduled(fixedDelay = 60_000)
    public void cancelExpiredRooms() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Long> roomIds = roomAutoCancelService.findExpiredRoomIds(now);
        int canceled = 0;
        for (Long roomId : roomIds) {
            try {
                if (roomAutoCancelService.cancelIfExpired(roomId, now)) {
                    canceled++;
                }
            } catch (RuntimeException exception) {
                log.error("Failed to auto-cancel expired room: roomId={}", roomId, exception);
            }
        }
        if (canceled > 0) {
            log.info("Auto-canceled expired rooms: count={}", canceled);
        }
    }
}
