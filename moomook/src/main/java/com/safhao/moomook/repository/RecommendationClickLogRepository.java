package com.safhao.moomook.repository;

import com.safhao.moomook.domain.RecommendationClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationClickLogRepository extends JpaRepository<RecommendationClickLog, Long> {
}
