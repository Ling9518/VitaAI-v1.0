package com.vitaai.repository;

import com.vitaai.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserIdAndTargetType(Long userId, String targetType);

    Optional<Favorite> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    long countByTargetTypeAndTargetId(String targetType, Long targetId);

    void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    void deleteByUserId(Long userId);
}
