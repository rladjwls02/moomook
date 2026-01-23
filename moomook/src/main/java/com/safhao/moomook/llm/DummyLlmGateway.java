package com.safhao.moomook.llm;

import com.safhao.moomook.domain.Menu;
import com.safhao.moomook.dto.ConstraintsDto;
import com.safhao.moomook.dto.ExplanationItemDto;
import com.safhao.moomook.dto.ExtractConstraintsResponse;
import com.safhao.moomook.dto.GenerateExplanationsResponse;
import java.util.ArrayList;
import java.util.List;
public class DummyLlmGateway implements LlmGateway {
    @Override
    public ExtractConstraintsResponse extractConstraints(String userText) {
        if (userText != null && userText.contains("LLM_FAIL")) {
            throw new IllegalStateException("Simulated LLM failure");
        }
        ConstraintsDto constraints = ConstraintsDto.defaultConstraints();
        if (userText != null) {
            constraints.setNotes(userText);
            if (userText.contains("매운") || userText.toLowerCase().contains("spicy")) {
                constraints.setSpicyPreference("hot");
                constraints.setSpicyMin(2);
                constraints.setSpicyMax(3);
            }
            if (userText.contains("덜 매워") || userText.contains("안 매운")) {
                constraints.setSpicyPreference("mild");
                constraints.setSpicyMin(0);
                constraints.setSpicyMax(1);
            }
            if (userText.contains("혼밥")) {
                constraints.getDesiredTags().add("혼밥");
            }
            if (userText.contains("빨리") || userText.contains("빠르게")) {
                constraints.setMaxCookTimeMin(15);
            }
        }
        return new ExtractConstraintsResponse(constraints, null);
    }

    @Override
    public GenerateExplanationsResponse generateExplanations(String userText, List<Menu> menus, ExtractConstraintsResponse constraints) {
        if (userText != null && userText.contains("LLM_FAIL")) {
            throw new IllegalStateException("Simulated LLM failure");
        }
        List<ExplanationItemDto> items = new ArrayList<>();
        for (Menu menu : menus) {
            String reason = "%s은(는) %s 스타일로 잘 맞아요.".formatted(menu.getName(),
                menu.getTags().isEmpty() ? "기본" : menu.getTags().get(0));
            items.add(new ExplanationItemDto(menu.getId(), reason));
        }
        return new GenerateExplanationsResponse(items, "요청하신 조건에 맞는 메뉴만 골랐어요.");
    }
}
