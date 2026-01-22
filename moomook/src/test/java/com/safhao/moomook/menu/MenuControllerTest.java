package com.safhao.moomook.menu;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuService menuService;

    @Test
    void createMenuReturnsResponse() throws Exception {
        MenuCreateRequest request = new MenuCreateRequest(
                "된장찌개",
                8500,
                "구수한 맛",
                Set.of("한식"),
                1,
                12,
                Set.of("대두"),
                Set.of("된장"),
                true,
                1,
                2
        );
        MenuResponse response = MenuResponse.builder()
                .id(1L)
                .name("된장찌개")
                .price(8500)
                .description("구수한 맛")
                .tags(Set.of("한식"))
                .spicyLevel(1)
                .cookTimeMin(12)
                .allergens(Set.of("대두"))
                .ingredients(Set.of("된장"))
                .available(true)
                .priorityScore(1)
                .popularityScore(2)
                .build();

        when(menuService.create(any(MenuCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("된장찌개"))
                .andExpect(jsonPath("$.price").value(8500));
    }

    @Test
    void listMenusReturnsArray() throws Exception {
        MenuResponse first = MenuResponse.builder()
                .id(1L)
                .name("칼국수")
                .price(7000)
                .description("쫄깃한 면")
                .tags(Set.of("한식"))
                .spicyLevel(0)
                .cookTimeMin(10)
                .allergens(Set.of())
                .ingredients(Set.of())
                .available(true)
                .priorityScore(0)
                .popularityScore(1)
                .build();
        MenuResponse second = MenuResponse.builder()
                .id(2L)
                .name("우동")
                .price(7500)
                .description("담백한 국물")
                .tags(Set.of("일식"))
                .spicyLevel(0)
                .cookTimeMin(8)
                .allergens(Set.of())
                .ingredients(Set.of())
                .available(true)
                .priorityScore(0)
                .popularityScore(1)
                .build();

        when(menuService.list()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/admin/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("우동"));
    }

    @Test
    void updateMenuReturnsResponse() throws Exception {
        MenuUpdateRequest request = new MenuUpdateRequest(
                "수정 메뉴",
                9900,
                null,
                Set.of("추천"),
                null,
                null,
                null,
                null,
                false,
                4,
                5
        );
        MenuResponse response = MenuResponse.builder()
                .id(5L)
                .name("수정 메뉴")
                .price(9900)
                .description("기존 설명")
                .tags(Set.of("추천"))
                .spicyLevel(1)
                .cookTimeMin(10)
                .allergens(Set.of())
                .ingredients(Set.of())
                .available(false)
                .priorityScore(4)
                .popularityScore(5)
                .build();

        when(menuService.update(eq(5L), any(MenuUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/menus/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.priorityScore").value(4));
    }
}
