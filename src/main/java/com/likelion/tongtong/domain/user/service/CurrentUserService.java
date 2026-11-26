package com.likelion.tongtong.domain.user.service;

import com.likelion.tongtong.domain.user.entity.User;
import com.likelion.tongtong.domain.user.repository.UserRepository;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUserService {

    private static final long SEED_USER_ID = 1L;

    private final UserRepository userRepository;

    public User getCurrentUser() {
        return userRepository.findById(SEED_USER_ID)
                .orElseThrow(() -> {
                    log.error("Seed user not found: userId={}", SEED_USER_ID);
                    return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
                });
    }
}
