package com.likelion.tongtong.domain.room.repository;

import com.likelion.tongtong.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
