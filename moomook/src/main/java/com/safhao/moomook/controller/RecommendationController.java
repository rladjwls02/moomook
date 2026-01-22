package com.safhao.moomook.controller;

import com.safhao.moomook.dto.RecommendRequest;
import com.safhao.moomook.dto.RecommendResponse;
import com.safhao.moomook.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores/{storeSlug}/tables/{tableCode}")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommendations")
    public ResponseEntity<RecommendResponse> recommend(@PathVariable String storeSlug,
                                                       @PathVariable String tableCode,
                                                       @RequestBody RecommendRequest request) {
        return ResponseEntity.ok(recommendationService.recommend(storeSlug, tableCode, request));
    }

    @PostMapping("/recommendations/{menuId}/click")
    public ResponseEntity<Void> logClick(@PathVariable String storeSlug,
                                         @PathVariable String tableCode,
                                         @PathVariable Long menuId,
                                         @RequestParam(defaultValue = "click") String action,
                                         @RequestParam(required = false) String sessionKey) {
        recommendationService.logClick(storeSlug, tableCode, menuId, action, sessionKey);
        return ResponseEntity.ok().build();
    }
}
