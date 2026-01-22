package com.safhao.moomook.dto;

import java.util.ArrayList;
import java.util.List;

public class RecommendResponse {
    private List<MenuRecommendationDto> items = new ArrayList<>();
    private String globalNote;
    private String followUpQuestion;
    private ConstraintsDto constraints;

    public List<MenuRecommendationDto> getItems() {
        return items;
    }

    public void setItems(List<MenuRecommendationDto> items) {
        this.items = items;
    }

    public String getGlobalNote() {
        return globalNote;
    }

    public void setGlobalNote(String globalNote) {
        this.globalNote = globalNote;
    }

    public String getFollowUpQuestion() {
        return followUpQuestion;
    }

    public void setFollowUpQuestion(String followUpQuestion) {
        this.followUpQuestion = followUpQuestion;
    }

    public ConstraintsDto getConstraints() {
        return constraints;
    }

    public void setConstraints(ConstraintsDto constraints) {
        this.constraints = constraints;
    }
}
