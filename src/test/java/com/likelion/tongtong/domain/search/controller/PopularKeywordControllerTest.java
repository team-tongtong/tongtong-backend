package com.likelion.tongtong.domain.search.controller;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.tongtong.domain.search.entity.PopularKeyword;
import com.likelion.tongtong.domain.search.repository.PopularKeywordRepository;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
class PopularKeywordControllerTest {

    private static final String PATH = "/api/v1/search/popular-keywords";

    private final MockMvc mockMvc;
    private final PopularKeywordRepository popularKeywordRepository;

    @Test
    @DisplayName("인기 검색어의 순위와 검색어를 순위 오름차순으로 최대 10개 반환한다")
    void 순위순_상위_10개_조회() throws Exception {
        // given
        popularKeywordRepository.deleteAllInBatch();
        List<PopularKeyword> popularKeywords = IntStream.iterate(11, rank -> rank > 0, rank -> rank - 1)
                .mapToObj(rank -> PopularKeyword.builder().ranking(rank).keyword("검색어 " + rank).build())
                .toList();
        popularKeywordRepository.saveAll(popularKeywords);

        // when & then
        mockMvc.perform(get(PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.keywords.length()").value(10))
                .andExpect(jsonPath("$.data.keywords[*].rank", contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)))
                .andExpect(jsonPath("$.data.keywords[0].keyword").value("검색어 1"))
                .andExpect(jsonPath("$.data.keywords[9].keyword").value("검색어 10"))
                .andExpect(jsonPath("$.data.keywords[0].ranking").doesNotExist());
    }

    @Test
    @DisplayName("저장된 인기 검색어가 없으면 성공 응답과 빈 배열을 반환한다")
    void 빈_목록_조회() throws Exception {
        // given
        popularKeywordRepository.deleteAllInBatch();

        // when & then
        mockMvc.perform(get(PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.keywords").isArray())
                .andExpect(jsonPath("$.data.keywords").isEmpty());
    }
}
