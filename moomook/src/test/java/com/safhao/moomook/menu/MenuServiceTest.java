package com.safhao.moomook.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    void createCopiesRequestData() {
        MenuCreateRequest request = MenuCreateRequest.builder()
                .name("불고기 덮밥")
                .price(8500)
                .description("달큰한 불고기")
                .tags(Set.of("한식", "혼밥"))
                .spicyLevel(1)
                .cookTimeMin(12)
                .allergens(Set.of("돼지고기"))
                .ingredients(Set.of("쌀", "불고기"))
                .available(true)
                .priorityScore(3)
                .popularityScore(7)
                .build();

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        when(menuRepository.save(captor.capture())).thenAnswer(invocation -> {
            Menu menu = invocation.getArgument(0);
            menu.setId(10L);
            return menu;
        });

        MenuResponse response = menuService.create(request);

        Menu saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("불고기 덮밥");
        assertThat(saved.getTags()).containsExactlyInAnyOrder("한식", "혼밥");
        assertThat(saved.getAllergens()).containsExactly("돼지고기");
        assertThat(saved.isAvailable()).isTrue();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.priorityScore()).isEqualTo(3);
    }

    @Test
    void listReturnsResponses() {
        Menu menu = Menu.builder()
                .id(1L)
                .name("김치찌개")
                .price(9000)
                .description("얼큰한 찌개")
                .tags(Set.of("한식"))
                .spicyLevel(4)
                .cookTimeMin(15)
                .allergens(Set.of("돼지고기"))
                .ingredients(Set.of("김치", "돼지고기"))
                .available(true)
                .priorityScore(2)
                .popularityScore(5)
                .build();

        when(menuRepository.findAll()).thenReturn(List.of(menu));

        List<MenuResponse> responses = menuService.list();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("김치찌개");
    }

    @Test
    void updateAppliesPatchFields() {
        Menu menu = Menu.builder()
                .id(3L)
                .name("순두부")
                .price(7000)
                .description("부드러운")
                .tags(Set.of("한식"))
                .spicyLevel(2)
                .cookTimeMin(8)
                .allergens(Set.of())
                .ingredients(Set.of("두부"))
                .available(true)
                .priorityScore(1)
                .popularityScore(2)
                .build();
        when(menuRepository.findById(3L)).thenReturn(java.util.Optional.of(menu));

        MenuUpdateRequest request = new MenuUpdateRequest(
                "순두부찌개",
                7500,
                null,
                Set.of("한식", "혼밥"),
                null,
                10,
                null,
                Set.of("두부", "김치"),
                false,
                3,
                null
        );

        MenuResponse response = menuService.update(3L, request);

        assertThat(response.name()).isEqualTo("순두부찌개");
        assertThat(response.price()).isEqualTo(7500);
        assertThat(response.tags().stream().sorted().collect(Collectors.toList()))
                .containsExactly("한식", "혼밥");
        assertThat(response.cookTimeMin()).isEqualTo(10);
        assertThat(response.available()).isFalse();
        assertThat(response.priorityScore()).isEqualTo(3);
    }
}
