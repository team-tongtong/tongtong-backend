package com.likelion.tongtong.domain.store.dto;

import com.likelion.tongtong.domain.store.entity.Store;
import java.util.List;

public record StoreListResponse(
        List<StoreSummaryResponse> stores
) {

    public static StoreListResponse from(List<Store> stores) {
        return new StoreListResponse(
                stores.stream()
                        .map(StoreSummaryResponse::from)
                        .toList()
        );
    }
}
