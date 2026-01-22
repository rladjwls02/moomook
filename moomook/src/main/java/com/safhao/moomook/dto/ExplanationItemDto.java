package com.safhao.moomook.dto;

public class ExplanationItemDto {
    private Long menuId;
    private String reason;

    public ExplanationItemDto() {
    }

    public ExplanationItemDto(Long menuId, String reason) {
        this.menuId = menuId;
        this.reason = reason;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
