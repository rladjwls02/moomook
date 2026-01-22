package com.safhao.moomook.recommendation;

import java.util.List;
import lombok.Builder;

@Builder
public record RecommendationResponse(
        String customerInput,
        List<RecommendationCandidate> candidates
) {
}
