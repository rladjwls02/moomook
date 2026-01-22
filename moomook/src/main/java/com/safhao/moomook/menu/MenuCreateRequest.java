package com.safhao.moomook.menu;

import java.util.Set;
import lombok.Builder;

@Builder
public record MenuCreateRequest(
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
}
