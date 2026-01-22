package com.safhao.moomook.engine;

import com.safhao.moomook.domain.Menu;
import com.safhao.moomook.dto.ConstraintsDto;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RecommendationEngineTest {
    @Test
    void recommend_excludesMenusWithAllergens_whenAllergyConstraintsProvided() {
        // given
        RecommendationEngine engine = new RecommendationEngine();
        Menu safeMenu = menu(1L, "안전", List.of("우유"), List.of("면"));
        Menu riskyMenu = menu(2L, "위험", List.of("땅콩"), List.of("마늘"));

        ConstraintsDto constraints = ConstraintsDto.defaultConstraints();
        constraints.getAllergens().add("땅콩");

        // when
        List<Menu> filtered = engine.recommend(List.of(safeMenu, riskyMenu), constraints, 5);

        // then
        Assertions.assertEquals(1, filtered.size(), "알레르기 조건과 일치하는 메뉴는 제외되어야 합니다.");
        Assertions.assertEquals("안전", filtered.get(0).getName(), "알레르기 메뉴가 제거되지 않았습니다.");
    }

    @Test
    void recommend_excludesMenusMarkedUnavailable_whenAvailabilityIsFalse() {
        // given
        RecommendationEngine engine = new RecommendationEngine();
        Menu availableMenu = menu(1L, "판매중", List.of(), List.of());
        Menu unavailableMenu = menu(2L, "품절", List.of(), List.of());
        unavailableMenu.setAvailable(false);

        ConstraintsDto constraints = ConstraintsDto.defaultConstraints();

        // when
        List<Menu> recommended = engine.recommend(List.of(availableMenu, unavailableMenu), constraints, 5);

        // then
        Assertions.assertEquals(1, recommended.size(), "판매중이 아닌 메뉴는 추천에서 제외되어야 합니다.");
        Assertions.assertEquals("판매중", recommended.get(0).getName(), "품절 메뉴가 추천에 포함되었습니다.");
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
