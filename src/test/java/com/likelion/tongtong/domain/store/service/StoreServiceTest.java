package com.likelion.tongtong.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.likelion.tongtong.domain.store.dto.MenuResponse;
import com.likelion.tongtong.domain.store.dto.ReviewResponse;
import com.likelion.tongtong.domain.store.dto.StoreDetailResponse;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Review;
import com.likelion.tongtong.domain.store.entity.Store;
import com.likelion.tongtong.domain.store.repository.MenuRepository;
import com.likelion.tongtong.domain.store.repository.ReviewRepository;
import com.likelion.tongtong.domain.store.repository.StoreRepository;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor
@TestConstructor(autowireMode = AutowireMode.ALL)
class StoreServiceTest {

    private final StoreService storeService;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final ReviewRepository reviewRepository;

    @Test
    @DisplayName("없는 가게 ID로 조회하면 STORE_NOT_FOUND로 거절한다")
    void rejectsUnknownStore() {
        // given
        Long unknownStoreId = 999L;

        // when & then
        assertThatThrownBy(() -> storeService.getStore(unknownStoreId))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_FOUND));
    }

    @Test
    @DisplayName("리뷰가 3개 이상이어도 ID 순으로 앞의 2개만 반환한다")
    void returnsOnlyFirstTwoReviews() {
        // given
        Store store = storeRepository.save(createStore("두찜 안산한대앞역점"));
        reviewRepository.save(createReview(store, "첫번째리뷰어"));
        reviewRepository.save(createReview(store, "두번째리뷰어"));
        reviewRepository.save(createReview(store, "세번째리뷰어"));

        // when
        StoreDetailResponse response = storeService.getStore(store.getId());

        // then
        assertThat(response.reviews())
                .extracting(ReviewResponse::nickname)
                .containsExactly("첫번째리뷰어", "두번째리뷰어");
    }

    @Test
    @DisplayName("요청한 가게의 메뉴만 ID 순으로 반환하고 다른 가게 메뉴는 섞이지 않는다")
    void returnsOnlyMenusOfRequestedStore() {
        // given
        Store store = storeRepository.save(createStore("두찜 안산한대앞역점"));
        Store otherStore = storeRepository.save(createStore("맥도날드 안산고잔DT점"));
        menuRepository.save(createMenu(store, "까만찜닭", true));
        menuRepository.save(createMenu(otherStore, "불고기 버거", false));
        menuRepository.save(createMenu(store, "콜라 1.25L", false));

        // when
        StoreDetailResponse response = storeService.getStore(store.getId());

        // then
        assertThat(response.menus())
                .extracting(MenuResponse::name)
                .containsExactly("까만찜닭", "콜라 1.25L");
    }

    private Store createStore(String name) {
        return Store.builder()
                .name(name)
                .category("한식")
                .imageUrl("https://example.com/store.png")
                .rating(new BigDecimal("4.9"))
                .reviewCount(259)
                .orderCount(1_000)
                .minOrderAmount(13_500)
                .deliveryFee(2_000)
                .deliveryMinutes(32)
                .distanceMeters(300)
                .lastOrderTime(LocalTime.of(23, 50))
                .build();
    }

    private Menu createMenu(Store store, String name, boolean portionable) {
        return Menu.builder()
                .store(store)
                .name(name)
                .description(null)
                .price(10_000)
                .imageUrl("https://example.com/menu.png")
                .portionable(portionable)
                .build();
    }

    private Review createReview(Store store, String nickname) {
        return Review.builder()
                .store(store)
                .nickname(nickname)
                .rating(5)
                .content("맛있어요")
                .build();
    }
}
