package com.safhao.moomook.repository;

import com.safhao.moomook.domain.Menu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByStoreIdAndIsAvailableTrue(Long storeId);

    Optional<Menu> findByIdAndStoreId(Long id, Long storeId);

    List<Menu> findByStoreId(Long storeId);
}
