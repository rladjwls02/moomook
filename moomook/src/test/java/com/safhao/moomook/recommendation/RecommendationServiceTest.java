package com.safhao.moomook.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.safhao.moomook.menu.Menu;
import com.safhao.moomook.menu.MenuRepository;
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
    void recommendFiltersAndScoresMenus() {
        Menu spicySolo = Menu.builder()
                .id(1L)
                .name("매운 닭갈비")
                .price(12000)
                .description("매운맛")
                .tags(Set.of("혼밥", "매운"))
                .spicyLevel(4)
                .cookTimeMin(12)
                .allergens(Set.of())
                .ingredients(Set.of("닭"))
                .available(true)
                .priorityScore(5)
                .popularityScore(3)
                .build();
        Menu mild = Menu.builder()
                .id(2L)
                .name("샐러드")
                .price(8000)
                .description("가벼운")
                .tags(Set.of("건강"))
                .spicyLevel(0)
                .cookTimeMin(5)
                .allergens(Set.of("견과"))
                .ingredients(Set.of("야채"))
                .available(true)
                .priorityScore(1)
                .popularityScore(1)
                .build();

        when(menuRepository.findAllByAvailableTrue()).thenReturn(List.of(spicySolo, mild));

        RecommendationRequest request = new RecommendationRequest(
                "매운 혼밥",
                3,
                null,
                15,
                15000,
                true,
                Set.of("매운"),
                null,
                null,
                Set.of("견과"),
                1
        );

        RecommendationResponse response = recommendationService.recommend(request);

        assertThat(response.candidates()).hasSize(1);
        RecommendationCandidate candidate = response.candidates().get(0);
        assertThat(candidate.menuId()).isEqualTo(1L);
        assertThat(candidate.reasons()).contains("혼밥 태그", "선호 태그 일치");
        assertThat(candidate.score()).isGreaterThan(0);
    }

    @Test
    void recommendReturnsEmptyWhenNoCandidates() {
        when(menuRepository.findAllByAvailableTrue()).thenReturn(List.of());

        RecommendationRequest request = new RecommendationRequest(
                "없음",
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null
        );

        RecommendationResponse response = recommendationService.recommend(request);

        assertThat(response.candidates()).isEmpty();
    }
}
