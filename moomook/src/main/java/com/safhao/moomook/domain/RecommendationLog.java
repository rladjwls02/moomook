package com.safhao.moomook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "recommendation_logs")
public class RecommendationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private TableEntity table;

    @Column(nullable = false)
    private String sessionKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String extractedConstraints;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendedMenuIds;

    @Column(nullable = false)
    private String modelProvider;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private int latencyMs;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public TableEntity getTable() {
        return table;
    }

    public void setTable(TableEntity table) {
        this.table = table;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public String getExtractedConstraints() {
        return extractedConstraints;
    }

    public void setExtractedConstraints(String extractedConstraints) {
        this.extractedConstraints = extractedConstraints;
    }

    public String getRecommendedMenuIds() {
        return recommendedMenuIds;
    }

    public void setRecommendedMenuIds(String recommendedMenuIds) {
        this.recommendedMenuIds = recommendedMenuIds;
    }

    public String getModelProvider() {
        return modelProvider;
    }

    public void setModelProvider(String modelProvider) {
        this.modelProvider = modelProvider;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(int latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
