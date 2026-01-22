package com.safhao.moomook.llm;

import com.safhao.moomook.domain.Menu;
import com.safhao.moomook.dto.ExtractConstraintsResponse;
import com.safhao.moomook.dto.GenerateExplanationsResponse;
import java.util.List;

public interface LlmGateway {
    ExtractConstraintsResponse extractConstraints(String userText);

    GenerateExplanationsResponse generateExplanations(String userText, List<Menu> menus, ExtractConstraintsResponse constraints);
}
