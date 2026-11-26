package com.likelion.tongtong.domain.room.repository;

import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
            select room
            from Room room
            join fetch room.store
            join fetch room.spot
            left join fetch room.sharedMenu
            where room.status = :status
              and room.recruitDeadline > :now
              and (:track is null or room.track = :track)
            order by room.recruitDeadline asc
            """)
    List<Room> findRecruitingRooms(@Param("status") RoomStatus status,
                                  @Param("now") LocalDateTime now,
                                  @Param("track") Track track);

    // 동시에 참여해도 정원을 넘지 않도록 모집방 행을 잠그고 조회한다
    // JPA 잠금이 만드는 for update of 문법은 DB마다 지원이 달라, 어디서나 되는 for update로 직접 쓴다
    @Query(value = "select * from room where room_id = :roomId for update", nativeQuery = true)
    Optional<Room> findByIdForUpdate(@Param("roomId") Long roomId);
}
