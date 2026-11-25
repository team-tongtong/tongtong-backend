package com.likelion.tongtong.domain.store.service;

import com.likelion.tongtong.domain.store.dto.StoreDetailResponse;
import com.likelion.tongtong.domain.store.dto.StoreListResponse;
import com.likelion.tongtong.domain.store.entity.Store;
import com.likelion.tongtong.domain.store.repository.MenuRepository;
import com.likelion.tongtong.domain.store.repository.ReviewRepository;
import com.likelion.tongtong.domain.store.repository.StoreRepository;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final ReviewRepository reviewRepository;

    public StoreListResponse getStores() {
        return StoreListResponse.from(storeRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
    }

    public StoreDetailResponse getStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        return StoreDetailResponse.from(
                store,
                menuRepository.findAllByStoreIdOrderByIdAsc(storeId),
                reviewRepository.findTop2ByStoreIdOrderByIdAsc(storeId)
        );
    }
}
