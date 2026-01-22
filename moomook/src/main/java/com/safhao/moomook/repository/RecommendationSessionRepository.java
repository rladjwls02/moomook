package com.safhao.moomook.repository;

import com.safhao.moomook.domain.RecommendationSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationSessionRepository extends JpaRepository<RecommendationSession, Long> {
    Optional<RecommendationSession> findByStoreIdAndSessionKey(Long storeId, String sessionKey);
}
