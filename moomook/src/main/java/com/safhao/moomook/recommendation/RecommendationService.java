package com.safhao.moomook.recommendation;

import com.safhao.moomook.menu.Menu;
import com.safhao.moomook.menu.MenuRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    private static final int DEFAULT_LIMIT = 5;

    private final MenuRepository menuRepository;

    public RecommendationService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        int limit = request.limit() != null ? request.limit() : DEFAULT_LIMIT;
        List<RecommendationCandidate> candidates = menuRepository.findAllByAvailableTrue().stream()
                .map(menu -> score(menu, request))
                .filter(scored -> scored != null)
                .sorted(Comparator.comparingInt(RecommendationCandidate::score).reversed())
                .limit(limit)
                .toList();
        return RecommendationResponse.builder()
                .customerInput(request.customerInput())
                .candidates(candidates)
                .build();
    }

    private RecommendationCandidate score(Menu menu, RecommendationRequest request) {
        if (request.budgetMax() != null && menu.getPrice() > request.budgetMax()) {
            return null;
        }
        if (request.maxCookTimeMin() != null && menu.getCookTimeMin() > request.maxCookTimeMin()) {
            return null;
        }
        if (request.spicyMin() != null && menu.getSpicyLevel() < request.spicyMin()) {
            return null;
        }
        if (request.spicyMax() != null && menu.getSpicyLevel() > request.spicyMax()) {
            return null;
        }
        if (!disjoint(menu.getAllergens(), request.excludeAllergens())) {
            return null;
        }
        if (!disjoint(menu.getIngredients(), request.excludeIngredients())) {
            return null;
        }
        if (!disjoint(menu.getTags(), request.excludeTags())) {
            return null;
        }
        if (request.includeTags() != null && !menu.getTags().containsAll(request.includeTags())) {
            return null;
        }
        if (Boolean.TRUE.equals(request.soloOk()) && !menu.getTags().contains("혼밥")) {
            return null;
        }

        int score = menu.getPriorityScore() + menu.getPopularityScore();
        List<String> reasons = new ArrayList<>();

        if (request.spicyMin() != null) {
            reasons.add("매운 정도 " + request.spicyMin() + " 이상");
            score += 10;
        }
        if (request.maxCookTimeMin() != null) {
            reasons.add("조리시간 " + request.maxCookTimeMin() + "분 이내");
            score += 8;
        }
        if (Boolean.TRUE.equals(request.soloOk())) {
            reasons.add("혼밥 태그");
            score += 6;
        }
        if (request.includeTags() != null && !request.includeTags().isEmpty()) {
            reasons.add("선호 태그 일치");
            score += 5 * request.includeTags().size();
        }
        if (menu.getPriorityScore() > 0) {
            reasons.add("사장님 우선순위 반영");
        }
        if (menu.getPopularityScore() > 0) {
            reasons.add("인기 메뉴");
        }

        return RecommendationCandidate.builder()
                .menuId(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .reasons(reasons)
                .score(score)
                .build();
    }

    private boolean disjoint(Set<String> menuValues, Set<String> excluded) {
        if (excluded == null || excluded.isEmpty()) {
            return true;
        }
        Set<String> menuSet = menuValues == null ? new HashSet<>() : menuValues;
        for (String value : excluded) {
            if (menuSet.contains(value)) {
                return false;
            }
        }
        return true;
    }
}
