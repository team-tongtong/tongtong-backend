package com.likelion.tongtong.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.likelion.tongtong.domain.user.entity.User;
import com.likelion.tongtong.domain.user.repository.UserRepository;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @Test
    @DisplayName("더미데이터에 지정된 사용자 ID로 현재 사용자를 조회한다")
    void returnsSeedUser() {
        // given
        User user = User.create();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        User currentUser = currentUserService.getCurrentUser();

        // then
        assertThat(currentUser).isSameAs(user);
    }

    @Test
    @DisplayName("더미데이터에 지정된 사용자가 없으면 서버 오류를 반환한다")
    void failsWhenSeedUserDoesNotExist() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when and then
        assertThatThrownBy(currentUserService::getCurrentUser)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
