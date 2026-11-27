package com.likelion.tongtong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TongtongApplicationTests {

    @Test
    @DisplayName("애플리케이션 컨텍스트가 정상적으로 뜬다")
    void contextLoads() {
    }

}
