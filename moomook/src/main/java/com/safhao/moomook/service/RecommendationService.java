package com.safhao.moomook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safhao.moomook.domain.ErrorLog;
import com.safhao.moomook.domain.Menu;
import com.safhao.moomook.domain.RecommendationClickLog;
import com.safhao.moomook.domain.RecommendationLog;
import com.safhao.moomook.domain.Store;
import com.safhao.moomook.domain.TableEntity;
import com.safhao.moomook.dto.ConstraintsDto;
import com.safhao.moomook.dto.ExplanationItemDto;
import com.safhao.moomook.dto.ExtractConstraintsResponse;
import com.safhao.moomook.dto.GenerateExplanationsResponse;
import com.safhao.moomook.dto.MenuRecommendationDto;
import com.safhao.moomook.dto.RecommendRequest;
import com.safhao.moomook.dto.RecommendResponse;
import com.safhao.moomook.engine.RecommendationEngine;
import com.safhao.moomook.llm.LlmGateway;
import com.safhao.moomook.repository.ErrorLogRepository;
import com.safhao.moomook.repository.MenuRepository;
import com.safhao.moomook.repository.RecommendationClickLogRepository;
import com.safhao.moomook.repository.RecommendationLogRepository;
import com.safhao.moomook.repository.StoreRepository;
import com.safhao.moomook.repository.TableRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {
    private final StoreRepository storeRepository;
    private final TableRepository tableRepository;
    private final MenuRepository menuRepository;
    private final RecommendationLogRepository recommendationLogRepository;
    private final RecommendationClickLogRepository recommendationClickLogRepository;
    private final ErrorLogRepository errorLogRepository;
    private final LlmGateway llmGateway;
    private final RecommendationEngine recommendationEngine = new RecommendationEngine();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecommendationService(StoreRepository storeRepository,
                                 TableRepository tableRepository,
                                 MenuRepository menuRepository,
                                 RecommendationLogRepository recommendationLogRepository,
                                 RecommendationClickLogRepository recommendationClickLogRepository,
                                 ErrorLogRepository errorLogRepository,
                                 LlmGateway llmGateway) {
        this.storeRepository = storeRepository;
        this.tableRepository = tableRepository;
        this.menuRepository = menuRepository;
        this.recommendationLogRepository = recommendationLogRepository;
        this.recommendationClickLogRepository = recommendationClickLogRepository;
        this.errorLogRepository = errorLogRepository;
        this.llmGateway = llmGateway;
    }

    public RecommendResponse recommend(String storeSlug, String tableCode, RecommendRequest request) {
        Store store = storeRepository.findBySlug(storeSlug)
            .orElseThrow(() -> new IllegalArgumentException("Store not found"));
        TableEntity table = resolveTable(store.getId(), tableCode);
        String sessionKey = Optional.ofNullable(request.getSessionKey()).orElse("guest");

        long startedAt = System.currentTimeMillis();
        ExtractConstraintsResponse extracted = extractConstraintsWithFallback(store, sessionKey, request.getUserText());
        List<Menu> availableMenus = menuRepository.findByStoreIdAndIsAvailableTrue(store.getId());

        int topN = Optional.ofNullable(request.getTopN()).orElse(5);
        List<Menu> recommendedMenus = recommendationEngine.recommend(availableMenus, extracted.getConstraints(), topN);

        GenerateExplanationsResponse explanations = generateExplanationsWithFallback(store, sessionKey, request.getUserText(),
            recommendedMenus, extracted);

        int latency = (int) (System.currentTimeMillis() - startedAt);
        saveRecommendationLog(store, table, sessionKey, request.getUserText(), extracted, recommendedMenus, latency);

        return buildResponse(recommendedMenus, explanations, extracted);
    }

    public void logClick(String storeSlug, String tableCode, Long menuId, String action, String sessionKey) {
        Store store = storeRepository.findBySlug(storeSlug)
            .orElseThrow(() -> new IllegalArgumentException("Store not found"));
        TableEntity table = resolveTable(store.getId(), tableCode);
        Menu menu = menuRepository.findByIdAndStoreId(menuId, store.getId())
            .orElseThrow(() -> new IllegalArgumentException("Menu not found"));
        RecommendationClickLog log = new RecommendationClickLog();
        log.setStore(store);
        log.setTable(table);
        log.setMenu(menu);
        log.setAction(action);
        log.setSessionKey(Optional.ofNullable(sessionKey).orElse("guest"));
        recommendationClickLogRepository.save(log);
    }

    private TableEntity resolveTable(Long storeId, String tableCode) {
        if (tableCode == null) {
            return null;
        }
        return tableRepository.findByStoreIdAndCode(storeId, tableCode).orElse(null);
    }

    private ExtractConstraintsResponse extractConstraintsWithFallback(Store store, String sessionKey, String userText) {
        try {
            ExtractConstraintsResponse response = llmGateway.extractConstraints(userText);
            if (response.getConstraints() == null) {
                throw new IllegalStateException("LLM constraints missing");
            }
            return response;
        } catch (RuntimeException ex) {
            logError(store, sessionKey, "LLM_CONSTRAINTS_FAILURE", ex.getMessage());
            ConstraintsDto fallback = ConstraintsDto.defaultConstraints();
            fallback.setNotes("LLM 실패로 기본 추천을 제공합니다.");
            return new ExtractConstraintsResponse(fallback, "매운 거 괜찮으세요?");
        }
    }

    private GenerateExplanationsResponse generateExplanationsWithFallback(Store store,
                                                                         String sessionKey,
                                                                         String userText,
                                                                         List<Menu> menus,
                                                                         ExtractConstraintsResponse constraints) {
        try {
            GenerateExplanationsResponse response = llmGateway.generateExplanations(userText, menus, constraints);
            if (response.getItems() == null || response.getItems().isEmpty()) {
                throw new IllegalStateException("LLM explanations missing");
            }
            return response;
        } catch (RuntimeException ex) {
            logError(store, sessionKey, "LLM_EXPLANATION_FAILURE", ex.getMessage());
            List<ExplanationItemDto> items = menus.stream()
                .map(menu -> new ExplanationItemDto(menu.getId(), fallbackReason(menu, constraints.getConstraints())))
                .toList();
            return new GenerateExplanationsResponse(items, "기본 규칙으로 추천했어요.");
        }
    }

    private void saveRecommendationLog(Store store,
                                       TableEntity table,
                                       String sessionKey,
                                       String userText,
                                       ExtractConstraintsResponse extracted,
                                       List<Menu> menus,
                                       int latencyMs) {
        RecommendationLog log = new RecommendationLog();
        log.setStore(store);
        log.setTable(table);
        log.setSessionKey(sessionKey);
        log.setUserText(Optional.ofNullable(userText).orElse(""));
        log.setExtractedConstraints(writeJson(extracted.getConstraints()));
        log.setRecommendedMenuIds(writeJson(menus.stream().map(Menu::getId).toList()));
        log.setModelProvider("dummy");
        log.setModelName("fallback-llm");
        log.setLatencyMs(latencyMs);
        recommendationLogRepository.save(log);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private void logError(Store store, String sessionKey, String errorType, String message) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setStore(store);
        errorLog.setSessionKey(sessionKey);
        errorLog.setErrorType(errorType);
        errorLog.setMessage(Optional.ofNullable(message).orElse(""));
        errorLog.setCreatedAt(Instant.now());
        errorLogRepository.save(errorLog);
    }

    private RecommendResponse buildResponse(List<Menu> menus,
                                            GenerateExplanationsResponse explanations,
                                            ExtractConstraintsResponse extracted) {
        Map<Long, String> reasons = new HashMap<>();
        if (explanations.getItems() != null) {
            for (ExplanationItemDto item : explanations.getItems()) {
                reasons.put(item.getMenuId(), item.getReason());
            }
        }
        List<MenuRecommendationDto> items = menus.stream().map(menu -> {
            MenuRecommendationDto dto = new MenuRecommendationDto();
            dto.setMenuId(menu.getId());
            dto.setName(menu.getName());
            dto.setDescription(menu.getDescription());
            dto.setPrice(menu.getPrice());
            dto.setReason(reasons.getOrDefault(menu.getId(), "추천 메뉴입니다."));
            return dto;
        }).collect(Collectors.toList());

        RecommendResponse response = new RecommendResponse();
        response.setItems(items);
        response.setGlobalNote(explanations.getGlobalNote());
        response.setFollowUpQuestion(extracted.getFollowUpQuestion());
        response.setConstraints(extracted.getConstraints());
        return response;
    }

    private String fallbackReason(Menu menu, ConstraintsDto constraints) {
        String tagPart = menu.getTags().isEmpty() ? "기본" : menu.getTags().get(0);
        String spicyPart = constraints.getSpicyPreference();
        return "%s 메뉴는 %s 느낌이고 %s 선호에 맞춰 추천했어요.".formatted(menu.getName(), tagPart,
            spicyPart == null ? "기본" : spicyPart);
    }
}
