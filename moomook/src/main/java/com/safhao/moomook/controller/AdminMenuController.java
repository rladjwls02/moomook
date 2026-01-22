package com.safhao.moomook.controller;

import com.safhao.moomook.dto.MenuRequest;
import com.safhao.moomook.dto.MenuResponse;
import com.safhao.moomook.service.MenuService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stores/{storeId}/menus")
public class AdminMenuController {
    private final MenuService menuService;

    public AdminMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<List<MenuResponse>> list(@PathVariable Long storeId) {
        return ResponseEntity.ok(menuService.listMenus(storeId));
    }

    @PostMapping
    public ResponseEntity<MenuResponse> create(@PathVariable Long storeId, @RequestBody MenuRequest request) {
        return ResponseEntity.ok(menuService.createMenu(storeId, request));
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> update(@PathVariable Long storeId,
                                               @PathVariable Long menuId,
                                               @RequestBody MenuRequest request) {
        return ResponseEntity.ok(menuService.updateMenu(storeId, menuId, request));
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> delete(@PathVariable Long storeId, @PathVariable Long menuId) {
        menuService.deleteMenu(storeId, menuId);
        return ResponseEntity.noContent().build();
    }
}
