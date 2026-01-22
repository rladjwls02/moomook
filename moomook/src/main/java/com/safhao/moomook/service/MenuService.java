package com.safhao.moomook.service;

import com.safhao.moomook.domain.Menu;
import com.safhao.moomook.domain.Store;
import com.safhao.moomook.dto.MenuRequest;
import com.safhao.moomook.dto.MenuResponse;
import com.safhao.moomook.repository.MenuRepository;
import com.safhao.moomook.repository.StoreRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MenuService {
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    public MenuService(StoreRepository storeRepository, MenuRepository menuRepository) {
        this.storeRepository = storeRepository;
        this.menuRepository = menuRepository;
    }

    public MenuResponse createMenu(Long storeId, MenuRequest request) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new IllegalArgumentException("Store not found"));
        Menu menu = new Menu();
        apply(menu, request);
        menu.setStore(store);
        return toResponse(menuRepository.save(menu));
    }

    public MenuResponse updateMenu(Long storeId, Long menuId, MenuRequest request) {
        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
            .orElseThrow(() -> new IllegalArgumentException("Menu not found"));
        apply(menu, request);
        return toResponse(menuRepository.save(menu));
    }

    public void deleteMenu(Long storeId, Long menuId) {
        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
            .orElseThrow(() -> new IllegalArgumentException("Menu not found"));
        menuRepository.delete(menu);
    }

    public List<MenuResponse> listMenus(Long storeId) {
        return menuRepository.findByStoreId(storeId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private void apply(Menu menu, MenuRequest request) {
        menu.setName(request.getName());
        menu.setPrice(request.getPrice());
        menu.setDescription(request.getDescription());
        menu.setTags(request.getTags() == null ? List.of() : request.getTags());
        menu.setSpicyLevel(request.getSpicyLevel());
        menu.setCookTimeMin(request.getCookTimeMin());
        menu.setAllergens(request.getAllergens() == null ? List.of() : request.getAllergens());
        menu.setIngredients(request.getIngredients() == null ? List.of() : request.getIngredients());
        menu.setAvailable(request.isAvailable());
        menu.setPriorityScore(request.getPriorityScore());
    }

    private MenuResponse toResponse(Menu menu) {
        MenuResponse response = new MenuResponse();
        response.setId(menu.getId());
        response.setName(menu.getName());
        response.setPrice(menu.getPrice());
        response.setDescription(menu.getDescription());
        response.setTags(menu.getTags());
        response.setSpicyLevel(menu.getSpicyLevel());
        response.setCookTimeMin(menu.getCookTimeMin());
        response.setAllergens(menu.getAllergens());
        response.setIngredients(menu.getIngredients());
        response.setAvailable(menu.isAvailable());
        response.setPriorityScore(menu.getPriorityScore());
        response.setPopularityScore(menu.getPopularityScore());
        return response;
    }
}
