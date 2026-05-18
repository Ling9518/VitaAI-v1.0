package com.vitaai.service;

import com.vitaai.entity.*;
import com.vitaai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final DiseaseRepository diseaseRepository;
    private final DrugRepository drugRepository;

    @Transactional
    public Map<String, Object> toggle(Long userId, String targetType, Long targetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Optional<Favorite> existing = favoriteRepository
                .findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);

        boolean isFavorited;
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            isFavorited = false;
        } else {
            Favorite fav = Favorite.builder()
                    .user(user)
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
            favoriteRepository.save(fav);
            isFavorited = true;
        }

        long count = favoriteRepository.countByTargetTypeAndTargetId(targetType, targetId);
        updateTargetFavoritesCount(targetType, targetId, count);

        Map<String, Object> result = new HashMap<>();
        result.put("isFavorited", isFavorited);
        result.put("favoritesCount", count);
        return result;
    }

    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        return favoriteRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    public List<Map<String, Object>> getUserFavorites(Long userId, String targetType) {
        List<Favorite> favs = favoriteRepository.findByUserIdAndTargetType(userId, targetType);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite fav : favs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", fav.getId());
            item.put("targetType", fav.getTargetType());
            item.put("targetId", fav.getTargetId());
            item.put("createdAt", fav.getCreatedAt());
            // Load target details
            if ("DISEASE".equals(targetType)) {
                diseaseRepository.findById(fav.getTargetId()).ifPresent(d -> {
                    item.put("name", d.getName());
                    item.put("classification", d.getClassification());
                });
            } else if ("DRUG".equals(targetType)) {
                drugRepository.findById(fav.getTargetId()).ifPresent(d -> {
                    item.put("name", d.getName());
                    item.put("drugType", d.getDrugType().name());
                });
            }
            result.add(item);
        }
        return result;
    }

    private void updateTargetFavoritesCount(String targetType, Long targetId, long count) {
        if ("DISEASE".equals(targetType)) {
            diseaseRepository.findById(targetId).ifPresent(d -> {
                d.setFavoritesCount((int) count);
                diseaseRepository.save(d);
            });
        } else if ("DRUG".equals(targetType)) {
            drugRepository.findById(targetId).ifPresent(d -> {
                d.setFavoritesCount((int) count);
                drugRepository.save(d);
            });
        }
    }
}
