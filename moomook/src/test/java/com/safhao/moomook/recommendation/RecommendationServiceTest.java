package com.safhao.moomook.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.safhao.moomook.menu.Menu;
import com.safhao.moomook.menu.MenuRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void recommendScoresAndFiltersCandidates() {
        Menu menu = Menu.builder()
                .id(1L)
                .name("매운 닭갈비")
                .price(11000)
                .description("매콤한 한상")
                .tags(Set.of("혼밥", "매운"))
                .spicyLevel(3)
                .cookTimeMin(12)
                .allergens(Set.of())
                .ingredients(Set.of("닭고기"))
                .available(true)
                .priorityScore(2)
                .popularityScore(4)
                .build();
        Menu slowMenu = Menu.builder()
                .id(2L)
                .name("오래 끓인 국")
                .price(9000)
                .description("진한 국물")
                .tags(Set.of("매운"))
                .spicyLevel(2)
                .cookTimeMin(40)
                .allergens(Set.of())
                .ingredients(Set.of("소고기"))
                .available(true)
                .priorityScore(1)
                .popularityScore(1)
                .build();

        when(menuRepository.findAllByAvailableTrue()).thenReturn(List.of(menu, slowMenu));

        RecommendationRequest request = new RecommendationRequest(
                "혼밥 매운 메뉴",
                2,
                null,
                15,
                12000,
                true,
                Set.of("매운"),
                null,
                null,
                null,
                null
        );

        RecommendationResponse response = recommendationService.recommend(request);

        assertEquals(1, response.candidates().size());
        RecommendationCandidate candidate = response.candidates().getFirst();
        assertEquals(1L, candidate.menuId());
        assertTrue(candidate.reasons().contains("매운 정도 2 이상"));
        assertTrue(candidate.reasons().contains("조리시간 15분 이내"));
        assertTrue(candidate.reasons().contains("혼밥 태그"));
        assertTrue(candidate.reasons().contains("선호 태그 일치"));
    }

    @Test
    void recommendExcludesByAllergensAndBudget() {
        Menu menu = Menu.builder()
                .id(3L)
                .name("새우 파스타")
                .price(15000)
                .description("해산물 듬뿍")
                .tags(Set.of("양식"))
                .spicyLevel(1)
                .cookTimeMin(15)
                .allergens(Set.of("새우"))
                .ingredients(Set.of("파스타"))
                .available(true)
                .priorityScore(0)
                .popularityScore(0)
                .build();

        when(menuRepository.findAllByAvailableTrue()).thenReturn(List.of(menu));

        RecommendationRequest request = new RecommendationRequest(
                "알러지 있음",
                null,
                null,
                null,
                10000,
                null,
                null,
                null,
                null,
                Set.of("새우"),
                null
        );

        RecommendationResponse response = recommendationService.recommend(request);

        assertTrue(response.candidates().isEmpty());
    }

    @Test
    void recommendUsesDefaultLimit() {
        List<Menu> menus = new ArrayList<>();
        for (long i = 1; i <= 6; i++) {
            menus.add(Menu.builder()
                    .id(i)
                    .name("메뉴 " + i)
                    .price(5000)
                    .description("설명")
                    .tags(Set.of("기본"))
                    .spicyLevel(1)
                    .cookTimeMin(5)
                    .allergens(Set.of())
                    .ingredients(Set.of("재료"))
                    .available(true)
                    .priorityScore(0)
                    .popularityScore(0)
                    .build());
        }

        when(menuRepository.findAllByAvailableTrue()).thenReturn(menus);

        RecommendationRequest request = new RecommendationRequest(
                "기본",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        RecommendationResponse response = recommendationService.recommend(request);

        assertEquals(5, response.candidates().size());
    }
}
