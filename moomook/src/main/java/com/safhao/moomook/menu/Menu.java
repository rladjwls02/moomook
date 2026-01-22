package com.safhao.moomook.menu;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "menu_tags", joinColumns = @JoinColumn(name = "menu_id"))
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    private int spicyLevel;

    private int cookTimeMin;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "menu_allergens", joinColumns = @JoinColumn(name = "menu_id"))
    @Builder.Default
    private Set<String> allergens = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "menu_ingredients", joinColumns = @JoinColumn(name = "menu_id"))
    @Builder.Default
    private Set<String> ingredients = new HashSet<>();

    private boolean available;

    private int priorityScore;

    private int popularityScore;
}
