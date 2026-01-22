package com.safhao.moomook.recommendation;

import java.util.List;
import lombok.Builder;

@Builder
public record RecommendationCandidate(
        Long menuId,
        String name,
        int price,
        List<String> reasons,
        int score
) {
}
