package com.safhao.moomook.menu;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public MenuResponse create(MenuCreateRequest request) {
        Menu menu = Menu.builder()
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .tags(copySet(request.tags()))
                .spicyLevel(request.spicyLevel())
                .cookTimeMin(request.cookTimeMin())
                .allergens(copySet(request.allergens()))
                .ingredients(copySet(request.ingredients()))
                .available(request.available())
                .priorityScore(request.priorityScore())
                .popularityScore(request.popularityScore())
                .build();
        return MenuResponse.from(menuRepository.save(menu));
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> list() {
        return menuRepository.findAll().stream()
                .map(MenuResponse::from)
                .toList();
    }

    public MenuResponse update(Long id, MenuUpdateRequest request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found"));

        Optional.ofNullable(request.name()).ifPresent(menu::setName);
        Optional.ofNullable(request.price()).ifPresent(menu::setPrice);
        Optional.ofNullable(request.description()).ifPresent(menu::setDescription);
        Optional.ofNullable(request.tags()).ifPresent(tags -> menu.setTags(copySet(tags)));
        Optional.ofNullable(request.spicyLevel()).ifPresent(menu::setSpicyLevel);
        Optional.ofNullable(request.cookTimeMin()).ifPresent(menu::setCookTimeMin);
        Optional.ofNullable(request.allergens()).ifPresent(allergens -> menu.setAllergens(copySet(allergens)));
        Optional.ofNullable(request.ingredients()).ifPresent(ingredients -> menu.setIngredients(copySet(ingredients)));
        Optional.ofNullable(request.available()).ifPresent(menu::setAvailable);
        Optional.ofNullable(request.priorityScore()).ifPresent(menu::setPriorityScore);
        Optional.ofNullable(request.popularityScore()).ifPresent(menu::setPopularityScore);

        return MenuResponse.from(menu);
    }

    private static HashSet<String> copySet(Set<String> source) {
        if (source == null) {
            return new HashSet<>();
        }
        return new HashSet<>(source);
    }
}
