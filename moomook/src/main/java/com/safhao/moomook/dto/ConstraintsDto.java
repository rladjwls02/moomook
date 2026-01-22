package com.safhao.moomook.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstraintsDto {
    @JsonProperty("spicy_preference")
    private String spicyPreference;
    @JsonProperty("spicy_min")
    private int spicyMin;
    @JsonProperty("spicy_max")
    private int spicyMax;
    @JsonProperty("desired_tags")
    private List<String> desiredTags = new ArrayList<>();
    @JsonProperty("avoid_tags")
    private List<String> avoidTags = new ArrayList<>();
    @JsonProperty("exclude_ingredients")
    private List<String> excludeIngredients = new ArrayList<>();
    @JsonProperty("allergens")
    private List<String> allergens = new ArrayList<>();
    @JsonProperty("max_cook_time_min")
    private Integer maxCookTimeMin;
    @JsonProperty("budget_max")
    private Integer budgetMax;
    @JsonProperty("portion_preference")
    private String portionPreference;
    @JsonProperty("notes")
    private String notes;

    public static ConstraintsDto defaultConstraints() {
        ConstraintsDto constraints = new ConstraintsDto();
        constraints.setSpicyPreference("any");
        constraints.setSpicyMin(0);
        constraints.setSpicyMax(3);
        constraints.setPortionPreference("any");
        constraints.setNotes("");
        return constraints;
    }

    public String getSpicyPreference() {
        return spicyPreference;
    }

    public void setSpicyPreference(String spicyPreference) {
        this.spicyPreference = spicyPreference;
    }

    public int getSpicyMin() {
        return spicyMin;
    }

    public void setSpicyMin(int spicyMin) {
        this.spicyMin = spicyMin;
    }

    public int getSpicyMax() {
        return spicyMax;
    }

    public void setSpicyMax(int spicyMax) {
        this.spicyMax = spicyMax;
    }

    public List<String> getDesiredTags() {
        return desiredTags;
    }

    public void setDesiredTags(List<String> desiredTags) {
        this.desiredTags = desiredTags;
    }

    public List<String> getAvoidTags() {
        return avoidTags;
    }

    public void setAvoidTags(List<String> avoidTags) {
        this.avoidTags = avoidTags;
    }

    public List<String> getExcludeIngredients() {
        return excludeIngredients;
    }

    public void setExcludeIngredients(List<String> excludeIngredients) {
        this.excludeIngredients = excludeIngredients;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public Integer getMaxCookTimeMin() {
        return maxCookTimeMin;
    }

    public void setMaxCookTimeMin(Integer maxCookTimeMin) {
        this.maxCookTimeMin = maxCookTimeMin;
    }

    public Integer getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(Integer budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getPortionPreference() {
        return portionPreference;
    }

    public void setPortionPreference(String portionPreference) {
        this.portionPreference = portionPreference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
