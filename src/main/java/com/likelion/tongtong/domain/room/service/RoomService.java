package com.likelion.tongtong.domain.room.service;

import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.order.repository.OrderRepository.RoomParticipantCount;
import com.likelion.tongtong.domain.room.dto.RoomListResponse;
import com.likelion.tongtong.domain.room.dto.RoomSummaryResponse;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import com.likelion.tongtong.domain.room.repository.RoomRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final OrderRepository orderRepository;
    private final Clock clock;

    public RoomListResponse getRooms(Track track) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Room> rooms = roomRepository.findRecruitingRooms(RoomStatus.RECRUITING, now, track);
        if (rooms.isEmpty()) {
            return new RoomListResponse(List.of());
        }

        List<Long> roomIds = rooms.stream().map(Room::getId).toList();
        Map<Long, Long> participantsByRoom = orderRepository.countByRoomIdsAndStatus(roomIds, OrderStatus.JOINED)
                .stream()
                .collect(Collectors.toMap(RoomParticipantCount::getRoomId, RoomParticipantCount::getParticipantCount));

        List<RoomSummaryResponse> summaries = rooms.stream()
                .map(room -> RoomSummaryResponse.from(room, participantsByRoom.getOrDefault(room.getId(), 0L)))
                .toList();
        return new RoomListResponse(summaries);
    }
}
