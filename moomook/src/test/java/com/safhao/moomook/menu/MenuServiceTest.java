package com.safhao.moomook.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void createCopiesNullSets() {
        MenuCreateRequest request = new MenuCreateRequest(
                "김치찌개",
                9000,
                "집밥 느낌",
                null,
                2,
                15,
                null,
                null,
                true,
                3,
                7
        );

        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> {
            Menu saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        MenuResponse response = menuService.create(request);

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuRepository).save(captor.capture());
        Menu saved = captor.getValue();

        assertEquals("김치찌개", saved.getName());
        assertTrue(saved.getTags().isEmpty());
        assertTrue(saved.getAllergens().isEmpty());
        assertTrue(saved.getIngredients().isEmpty());
        assertEquals(1L, response.id());
        assertEquals("김치찌개", response.name());
        assertTrue(response.tags().isEmpty());
    }

    @Test
    void listReturnsAllMenus() {
        Menu first = Menu.builder()
                .id(1L)
                .name("비빔밥")
                .price(8000)
                .description("든든한 한 끼")
                .tags(Set.of("한식"))
                .spicyLevel(1)
                .cookTimeMin(10)
                .allergens(Set.of("계란"))
                .ingredients(Set.of("밥"))
                .available(true)
                .priorityScore(1)
                .popularityScore(5)
                .build();
        Menu second = Menu.builder()
                .id(2L)
                .name("돈까스")
                .price(10000)
                .description("바삭한 식감")
                .tags(Set.of("일식"))
                .spicyLevel(0)
                .cookTimeMin(12)
                .allergens(Set.of("돼지고기"))
                .ingredients(Set.of("빵가루"))
                .available(true)
                .priorityScore(0)
                .popularityScore(2)
                .build();

        when(menuRepository.findAll()).thenReturn(List.of(first, second));

        List<MenuResponse> responses = menuService.list();

        assertEquals(2, responses.size());
        assertEquals("비빔밥", responses.get(0).name());
        assertEquals("돈까스", responses.get(1).name());
    }

    @Test
    void updateAppliesProvidedFields() {
        Menu menu = Menu.builder()
                .id(10L)
                .name("초기 메뉴")
                .price(12000)
                .description("기본 설명")
                .tags(Set.of("기본"))
                .spicyLevel(1)
                .cookTimeMin(20)
                .allergens(Set.of("콩"))
                .ingredients(Set.of("두부"))
                .available(true)
                .priorityScore(0)
                .popularityScore(1)
                .build();

        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));

        MenuUpdateRequest request = new MenuUpdateRequest(
                "업데이트 메뉴",
                null,
                null,
                Set.of("새태그"),
                3,
                null,
                null,
                null,
                false,
                5,
                null
        );

        MenuResponse response = menuService.update(10L, request);

        assertEquals("업데이트 메뉴", response.name());
        assertEquals(12000, response.price());
        assertEquals(Set.of("새태그"), response.tags());
        assertEquals(3, response.spicyLevel());
        assertEquals(false, response.available());
        assertEquals(5, response.priorityScore());
    }

    @Test
    void updateThrowsWhenMenuMissing() {
        when(menuRepository.findById(99L)).thenReturn(Optional.empty());

        MenuUpdateRequest request = new MenuUpdateRequest(
                "없는 메뉴",
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

        assertThrows(IllegalArgumentException.class, () -> menuService.update(99L, request));
    }
}
