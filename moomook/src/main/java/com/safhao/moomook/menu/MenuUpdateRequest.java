package com.safhao.moomook.menu;

import java.util.Set;

public record MenuUpdateRequest(
        String name,
        Integer price,
        String description,
        Set<String> tags,
        Integer spicyLevel,
        Integer cookTimeMin,
        Set<String> allergens,
        Set<String> ingredients,
        Boolean available,
        Integer priorityScore,
        Integer popularityScore
) {
}
