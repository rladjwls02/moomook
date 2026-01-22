package com.safhao.moomook.recommendation;

import java.util.Set;

public record RecommendationRequest(
        String customerInput,
        Integer spicyMin,
        Integer spicyMax,
        Integer maxCookTimeMin,
        Integer budgetMax,
        Boolean soloOk,
        Set<String> includeTags,
        Set<String> excludeTags,
        Set<String> excludeIngredients,
        Set<String> excludeAllergens,
        Integer limit
) {
}
