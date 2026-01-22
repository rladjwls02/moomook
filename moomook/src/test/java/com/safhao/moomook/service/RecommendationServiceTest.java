package com.safhao.moomook.service;

import com.safhao.moomook.domain.Menu;
import com.safhao.moomook.domain.Store;
import com.safhao.moomook.dto.ConstraintsDto;
import com.safhao.moomook.dto.ExplanationItemDto;
import com.safhao.moomook.dto.ExtractConstraintsResponse;
import com.safhao.moomook.dto.GenerateExplanationsResponse;
import com.safhao.moomook.dto.RecommendRequest;
import com.safhao.moomook.dto.RecommendResponse;
import com.safhao.moomook.llm.LlmGateway;
import com.safhao.moomook.repository.ErrorLogRepository;
import com.safhao.moomook.repository.MenuRepository;
import com.safhao.moomook.repository.RecommendationClickLogRepository;
import com.safhao.moomook.repository.RecommendationLogRepository;
import com.safhao.moomook.repository.StoreRepository;
import com.safhao.moomook.repository.TableRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    @DisplayName("설명에 없는 메뉴 ID가 있어도 존재하는 메뉴만 추천한다")
    @Test
    void recommend_doesNotIncludeUnknownMenuIds_whenExplanationsContainNonexistentId() {
        // given
        StoreRepository storeRepository = mock(StoreRepository.class);
        TableRepository tableRepository = mock(TableRepository.class);
        MenuRepository menuRepository = mock(MenuRepository.class);
        RecommendationLogRepository recommendationLogRepository = mock(RecommendationLogRepository.class);
        RecommendationClickLogRepository recommendationClickLogRepository = mock(RecommendationClickLogRepository.class);
        ErrorLogRepository errorLogRepository = mock(ErrorLogRepository.class);
        LlmGateway llmGateway = mock(LlmGateway.class);
        RecommendationService service = new RecommendationService(storeRepository, tableRepository, menuRepository,
            recommendationLogRepository, recommendationClickLogRepository, errorLogRepository, llmGateway);

        Store store = storeWithId(10L, "store");
        Menu menuA = menuWithId(1L, "김치찌개");
        Menu menuB = menuWithId(2L, "된장찌개");

        when(storeRepository.findBySlug("store")).thenReturn(Optional.of(store));
        when(menuRepository.findByStoreIdAndIsAvailableTrue(anyLong())).thenReturn(List.of(menuA, menuB));
        when(llmGateway.extractConstraints(anyString()))
            .thenReturn(new ExtractConstraintsResponse(ConstraintsDto.defaultConstraints(), null));
        when(llmGateway.generateExplanations(anyString(), any(), any()))
            .thenReturn(new GenerateExplanationsResponse(List.of(
                new ExplanationItemDto(1L, "기본 추천"),
                new ExplanationItemDto(999L, "DB에 없는 메뉴")
            ), "note"));

        RecommendRequest request = new RecommendRequest();
        request.setUserText("추천해줘");
        request.setTopN(5);

        // when
        RecommendResponse response = service.recommend("store", null, request);

        // then
        List<Long> responseIds = response.getItems().stream()
            .map(item -> item.getMenuId())
            .toList();
        Assertions.assertTrue(responseIds.containsAll(List.of(1L, 2L)),
            "DB에 존재하는 메뉴는 추천 결과에 포함되어야 합니다.");
        Assertions.assertFalse(responseIds.contains(999L),
            "DB에 존재하지 않는 메뉴 ID가 추천에 포함되었습니다.");
    }

    @DisplayName("LLM 조건 추출 실패 시 폴백 제약을 사용한다")
    @Test
    void recommend_usesFallbackConstraints_whenLlmConstraintExtractionFails() {
        // given
        StoreRepository storeRepository = mock(StoreRepository.class);
        TableRepository tableRepository = mock(TableRepository.class);
        MenuRepository menuRepository = mock(MenuRepository.class);
        RecommendationLogRepository recommendationLogRepository = mock(RecommendationLogRepository.class);
        RecommendationClickLogRepository recommendationClickLogRepository = mock(RecommendationClickLogRepository.class);
        ErrorLogRepository errorLogRepository = mock(ErrorLogRepository.class);
        LlmGateway llmGateway = mock(LlmGateway.class);
        RecommendationService service = new RecommendationService(storeRepository, tableRepository, menuRepository,
            recommendationLogRepository, recommendationClickLogRepository, errorLogRepository, llmGateway);

        Store store = storeWithId(20L, "fallback-store");
        Menu menu = menuWithId(3L, "라면");

        when(storeRepository.findBySlug("fallback-store")).thenReturn(Optional.of(store));
        when(menuRepository.findByStoreIdAndIsAvailableTrue(anyLong())).thenReturn(List.of(menu));
        when(llmGateway.extractConstraints(anyString())).thenThrow(new RuntimeException("LLM failure"));
        when(llmGateway.generateExplanations(anyString(), any(), any()))
            .thenReturn(new GenerateExplanationsResponse(
                List.of(new ExplanationItemDto(3L, "기본 추천")), "note"));

        RecommendRequest request = new RecommendRequest();
        request.setUserText("추천해줘");

        // when
        RecommendResponse response = service.recommend("fallback-store", null, request);

        // then
        Assertions.assertEquals("매운 거 괜찮으세요?", response.getFollowUpQuestion(),
            "LLM 조건 추출 실패 시 폴백 질문이 제공되어야 합니다.");
        Assertions.assertTrue(response.getConstraints().getNotes().contains("LLM 실패"),
            "LLM 조건 추출 실패 시 폴백 제약 조건이 사용되어야 합니다.");
        Assertions.assertEquals(1, response.getItems().size(),
            "LLM 실패 시에도 추천 결과는 제공되어야 합니다.");
    }

    private Store storeWithId(Long id, String slug) {
        Store store = new Store();
        store.setName("테스트 매장");
        store.setSlug(slug);
        setId(Store.class, store, id);
        return store;
    }

    private Menu menuWithId(Long id, String name) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setDescription("설명");
        menu.setPrice(10000);
        menu.setSpicyLevel(1);
        menu.setCookTimeMin(10);
        menu.setAvailable(true);
        setId(Menu.class, menu, id);
        return menu;
    }

    private void setId(Class<?> type, Object target, Long id) {
        try {
            Field field = type.getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("테스트 ID 설정 실패", ex);
        }
    }
}
