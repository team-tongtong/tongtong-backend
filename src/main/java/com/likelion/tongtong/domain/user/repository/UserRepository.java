package com.likelion.tongtong.domain.user.repository;

import com.likelion.tongtong.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
