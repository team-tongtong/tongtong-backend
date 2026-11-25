package com.likelion.tongtong.domain.store.service;

import com.likelion.tongtong.domain.store.dto.StoreListResponse;
import com.likelion.tongtong.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreListResponse getStores() {
        return StoreListResponse.from(storeRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
    }
}
