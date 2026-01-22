package com.safhao.moomook.dto;

import java.util.ArrayList;
import java.util.List;

public class GenerateExplanationsResponse {
    private List<ExplanationItemDto> items = new ArrayList<>();
    private String globalNote;

    public GenerateExplanationsResponse() {
    }

    public GenerateExplanationsResponse(List<ExplanationItemDto> items, String globalNote) {
        this.items = items;
        this.globalNote = globalNote;
    }

    public List<ExplanationItemDto> getItems() {
        return items;
    }

    public void setItems(List<ExplanationItemDto> items) {
        this.items = items;
    }

    public String getGlobalNote() {
        return globalNote;
    }

    public void setGlobalNote(String globalNote) {
        this.globalNote = globalNote;
    }
}
