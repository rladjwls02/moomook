package com.safhao.moomook.menu;

import java.util.Set;
import lombok.Builder;

@Builder
public record MenuResponse(
        Long id,
        String name,
        int price,
        String description,
        Set<String> tags,
        int spicyLevel,
        int cookTimeMin,
        Set<String> allergens,
        Set<String> ingredients,
        boolean available,
        int priorityScore,
        int popularityScore
) {
    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .tags(menu.getTags())
                .spicyLevel(menu.getSpicyLevel())
                .cookTimeMin(menu.getCookTimeMin())
                .allergens(menu.getAllergens())
                .ingredients(menu.getIngredients())
                .available(menu.isAvailable())
                .priorityScore(menu.getPriorityScore())
                .popularityScore(menu.getPopularityScore())
                .build();
    }
}
