package com.safhao.moomook.engine;

import com.safhao.moomook.domain.Menu;
import com.safhao.moomook.dto.ConstraintsDto;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RecommendationEngineTest {
    @Test
    void filtersByAllergensAndIngredients() {
        RecommendationEngine engine = new RecommendationEngine();
        Menu safeMenu = menu(1L, "안전", List.of("면"), List.of("우유"));
        Menu riskyMenu = menu(2L, "위험", List.of("땅콩"), List.of("마늘"));

        ConstraintsDto constraints = ConstraintsDto.defaultConstraints();
        constraints.getAllergens().add("땅콩");
        constraints.getExcludeIngredients().add("마늘");

        List<Menu> filtered = engine.filterMenus(List.of(safeMenu, riskyMenu), constraints);

        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("안전", filtered.get(0).getName());
    }

    @Test
    void scoresAndSelectsTopMenus() {
        RecommendationEngine engine = new RecommendationEngine();
        Menu first = menu(1L, "첫번째", List.of("혼밥"), List.of());
        Menu second = menu(2L, "두번째", List.of("든든"), List.of());
        first.setPriorityScore(10);
        second.setPriorityScore(1);

        ConstraintsDto constraints = ConstraintsDto.defaultConstraints();
        constraints.getDesiredTags().add("혼밥");

        List<Menu> recommended = engine.recommend(List.of(first, second), constraints, 1);

        Assertions.assertEquals(1, recommended.size());
        Assertions.assertEquals("첫번째", recommended.get(0).getName());
    }

    private Menu menu(Long id, String name, List<String> allergens, List<String> ingredients) {
        Menu menu = new Menu();
        menu.setAvailable(true);
        menu.setName(name);
        menu.setDescription("desc");
        menu.setPrice(10000);
        menu.setCookTimeMin(10);
        menu.setSpicyLevel(1);
        menu.setAllergens(allergens);
        menu.setIngredients(ingredients);
        menu.setPriorityScore(0);
        menu.setPopularityScore(0);
        return menu;
    }
}
