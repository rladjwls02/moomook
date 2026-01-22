package com.safhao.moomook.menu;

import static org.mockito.ArgumentMatchers.any;
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
    void createReturnsMenuResponse() throws Exception {
        MenuCreateRequest request = MenuCreateRequest.builder()
                .name("제육볶음")
                .price(9500)
                .description("매콤한 제육")
                .tags(Set.of("한식"))
                .spicyLevel(3)
                .cookTimeMin(14)
                .allergens(Set.of("돼지고기"))
                .ingredients(Set.of("돼지고기", "고추장"))
                .available(true)
                .priorityScore(2)
                .popularityScore(4)
                .build();
        MenuResponse response = MenuResponse.builder()
                .id(11L)
                .name("제육볶음")
                .price(9500)
                .description("매콤한 제육")
                .tags(Set.of("한식"))
                .spicyLevel(3)
                .cookTimeMin(14)
                .allergens(Set.of("돼지고기"))
                .ingredients(Set.of("돼지고기", "고추장"))
                .available(true)
                .priorityScore(2)
                .popularityScore(4)
                .build();

        when(menuService.create(any(MenuCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.name").value("제육볶음"));
    }

    @Test
    void listReturnsMenus() throws Exception {
        List<MenuResponse> responses = List.of(
                MenuResponse.builder()
                        .id(1L)
                        .name("라면")
                        .price(5000)
                        .description("간단 메뉴")
                        .tags(Set.of("분식"))
                        .spicyLevel(2)
                        .cookTimeMin(5)
                        .allergens(Set.of("밀"))
                        .ingredients(Set.of("면"))
                        .available(true)
                        .priorityScore(1)
                        .popularityScore(1)
                        .build()
        );
        when(menuService.list()).thenReturn(responses);

        mockMvc.perform(get("/api/admin/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("라면"));
    }

    @Test
    void updateReturnsUpdatedMenu() throws Exception {
        MenuUpdateRequest request = new MenuUpdateRequest(
                "라볶이",
                6500,
                "달콤한 라볶이",
                Set.of("분식"),
                2,
                7,
                Set.of("밀"),
                Set.of("면", "떡"),
                true,
                2,
                3
        );
        MenuResponse response = MenuResponse.builder()
                .id(2L)
                .name("라볶이")
                .price(6500)
                .description("달콤한 라볶이")
                .tags(Set.of("분식"))
                .spicyLevel(2)
                .cookTimeMin(7)
                .allergens(Set.of("밀"))
                .ingredients(Set.of("면", "떡"))
                .available(true)
                .priorityScore(2)
                .popularityScore(3)
                .build();

        when(menuService.update(any(Long.class), any(MenuUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/menus/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("라볶이"))
                .andExpect(jsonPath("$.price").value(6500));
    }
}
