package com.safhao.moomook.dto;

public class ExtractConstraintsResponse {
    private ConstraintsDto constraints;
    private String followUpQuestion;

    public ExtractConstraintsResponse() {
    }

    public ExtractConstraintsResponse(ConstraintsDto constraints, String followUpQuestion) {
        this.constraints = constraints;
        this.followUpQuestion = followUpQuestion;
    }

    public ConstraintsDto getConstraints() {
        return constraints;
    }

    public void setConstraints(ConstraintsDto constraints) {
        this.constraints = constraints;
    }

    public String getFollowUpQuestion() {
        return followUpQuestion;
    }

    public void setFollowUpQuestion(String followUpQuestion) {
        this.followUpQuestion = followUpQuestion;
    }
}
