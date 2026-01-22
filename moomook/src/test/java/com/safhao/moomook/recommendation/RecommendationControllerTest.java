package com.safhao.moomook.recommendation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    void recommendReturnsCandidates() throws Exception {
        RecommendationRequest request = new RecommendationRequest(
                "추천",
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

        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .menuId(1L)
                .name("김치볶음밥")
                .price(7000)
                .reasons(List.of("인기 메뉴"))
                .score(12)
                .build();

        RecommendationResponse response = RecommendationResponse.builder()
                .customerInput("추천")
                .candidates(List.of(candidate))
                .build();

        when(recommendationService.recommend(any(RecommendationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerInput").value("추천"))
                .andExpect(jsonPath("$.candidates[0].menuId").value(1))
                .andExpect(jsonPath("$.candidates[0].score").value(12));
    }
}
