package com.safhao.moomook.engine;

import com.safhao.moomook.domain.Menu;
import com.safhao.moomook.dto.ConstraintsDto;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecommendationEngine {
    public List<Menu> recommend(List<Menu> menus, ConstraintsDto constraints, int topN) {
        List<Menu> filtered = filterMenus(menus, constraints);
        Map<Menu, Integer> scores = scoreMenus(filtered, constraints);
        return selectTopN(scores, topN);
    }

    public List<Menu> filterMenus(List<Menu> menus, ConstraintsDto constraints) {
        Set<String> allergenSet = normalize(constraints.getAllergens());
        Set<String> excludeSet = normalize(constraints.getExcludeIngredients());
        Set<String> avoidTags = normalize(constraints.getAvoidTags());
        return menus.stream()
            .filter(Menu::isAvailable)
            .filter(menu -> allergenSet.isEmpty() || menu.getAllergens().stream()
                .map(this::normalizeToken)
                .noneMatch(allergenSet::contains))
            .filter(menu -> excludeSet.isEmpty() || menu.getIngredients().stream()
                .map(this::normalizeToken)
                .noneMatch(excludeSet::contains))
            .filter(menu -> avoidTags.isEmpty() || menu.getTags().stream()
                .map(this::normalizeToken)
                .noneMatch(avoidTags::contains))
            .filter(menu -> constraints.getBudgetMax() == null || menu.getPrice() <= constraints.getBudgetMax())
            .filter(menu -> constraints.getMaxCookTimeMin() == null || menu.getCookTimeMin() <= constraints.getMaxCookTimeMin())
            .filter(menu -> menu.getSpicyLevel() >= constraints.getSpicyMin()
                && menu.getSpicyLevel() <= constraints.getSpicyMax())
            .toList();
    }

    public Map<Menu, Integer> scoreMenus(List<Menu> menus, ConstraintsDto constraints) {
        Set<String> desiredTags = normalize(constraints.getDesiredTags());
        return menus.stream().collect(Collectors.toMap(Function.identity(), menu -> {
            int score = 0;
            score += menu.getPriorityScore();
            score += menu.getPopularityScore();
            score += tagMatchScore(menu, desiredTags);
            score += spicyPreferenceScore(menu.getSpicyLevel(), constraints.getSpicyPreference());
            score += cookTimeScore(menu.getCookTimeMin(), constraints.getMaxCookTimeMin());
            return score;
        }));
    }

    public List<Menu> selectTopN(Map<Menu, Integer> scores, int topN) {
        return scores.entrySet().stream()
            .sorted(Map.Entry.<Menu, Integer>comparingByValue().reversed()
                .thenComparing(entry -> entry.getKey().getPriorityScore(), Comparator.reverseOrder()))
            .limit(topN)
            .map(Map.Entry::getKey)
            .toList();
    }

    private int tagMatchScore(Menu menu, Set<String> desiredTags) {
        if (desiredTags.isEmpty()) {
            return 0;
        }
        Set<String> menuTags = normalize(menu.getTags());
        int matches = 0;
        for (String tag : desiredTags) {
            if (menuTags.contains(tag)) {
                matches += 10;
            }
        }
        return matches;
    }

    private int spicyPreferenceScore(int spicyLevel, String preference) {
        String pref = Optional.ofNullable(preference).orElse("any");
        return switch (pref) {
            case "mild" -> spicyLevel <= 1 ? 8 : -4;
            case "medium" -> spicyLevel == 2 ? 8 : -2;
            case "hot" -> spicyLevel >= 2 ? 8 : -4;
            default -> 0;
        };
    }

    private int cookTimeScore(int cookTime, Integer maxCookTime) {
        if (maxCookTime == null) {
            return 0;
        }
        if (cookTime <= maxCookTime) {
            return 6;
        }
        return Math.max(-6, (maxCookTime - cookTime));
    }

    private Set<String> normalize(List<String> values) {
        if (values == null) {
            return Set.of();
        }
        return values.stream()
            .filter(Objects::nonNull)
            .map(this::normalizeToken)
            .collect(Collectors.toSet());
    }

    private String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.KOREAN);
    }
}
