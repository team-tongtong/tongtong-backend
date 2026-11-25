package com.likelion.tongtong.global.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class CorsConfigTest {

    private static final String PATH = "/v3/api-docs";

    private static ResultActions preflight(MockMvc mockMvc, String origin) throws Exception {
        return mockMvc.perform(options(PATH)
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"));
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @DisplayName("application.yml의 허용 출처를 적용할 때")
    @RequiredArgsConstructor
    @TestConstructor(autowireMode = AutowireMode.ALL)
    class 허용_출처 {

        private final MockMvc mockMvc;

        @ParameterizedTest
        @ValueSource(strings = {
                "http://localhost:3000",
                "http://localhost:5173",
                "https://tongtong-frontend.vercel.app"
        })
        @DisplayName("등록된 출처는 허용한다")
        void 등록된_출처_허용(String origin) throws Exception {
            // given & when & then
            preflight(mockMvc, origin)
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "https://evil.example.com",
                "https://tongtong-git-feat-abc.vercel.app"
        })
        @DisplayName("등록되지 않은 출처는 403으로 거부한다")
        void 등록되지_않은_출처_거부(String origin) throws Exception {
            // given & when & then
            preflight(mockMvc, origin)
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }

    @Test
    @DisplayName("허용 출처가 하나도 없으면 기동 시 예외를 던진다")
    void 허용_출처_없으면_예외() {
        // given
        String[] allowedOrigins = {"", " "};

        // when & then
        assertThatThrownBy(() -> new CorsConfig(allowedOrigins))
                .isInstanceOf(IllegalStateException.class);
    }
}
