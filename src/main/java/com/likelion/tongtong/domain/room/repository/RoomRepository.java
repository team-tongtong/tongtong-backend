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
            where room.id = :roomId
            """)
    Optional<Room> findDetailById(@Param("roomId") Long roomId);

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
}
