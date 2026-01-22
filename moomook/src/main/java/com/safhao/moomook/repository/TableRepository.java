package com.safhao.moomook.repository;

import com.safhao.moomook.domain.TableEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRepository extends JpaRepository<TableEntity, Long> {
    Optional<TableEntity> findByStoreIdAndCode(Long storeId, String code);
}
