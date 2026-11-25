package com.likelion.tongtong.domain.store.dto;

import com.likelion.tongtong.domain.store.entity.Menu;

public record MenuResponse(
        Long menuId,
        String name,
        String description,
        Integer price,
        String imageUrl,
        boolean portionable
) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getPrice(),
                menu.getImageUrl(),
                menu.isPortionable()
        );
    }
}
