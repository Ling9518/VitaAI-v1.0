package com.vitaai.service;

import com.vitaai.entity.Disease;
import com.vitaai.entity.User;
import com.vitaai.repository.DiseaseRepository;
import com.vitaai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;
    private final UserRepository userRepository;

    public Page<Disease> getDiseases(int page, int pageSize, String keyword, String classification) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "viewsCount"));
        if (keyword != null && !keyword.isEmpty()) {
            return diseaseRepository.search(keyword, pageable);
        }
        if (classification != null && !classification.isEmpty()) {
            return diseaseRepository.findByClassificationAndStatus(classification, Disease.Status.APPROVED, pageable);
        }
        return diseaseRepository.findAllApproved(pageable);
    }

    public Disease getDisease(Long id) {
        Disease disease = diseaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("疾病不存在"));
        Integer current = disease.getViewsCount();
        disease.setViewsCount((current == null ? 0 : current) + 1);
        diseaseRepository.save(disease);
        return disease;
    }

    @Transactional
    public Disease createDisease(Disease disease, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        disease.setCreatedBy(user);
        User.Role role = user.getRole();
        disease.setStatus(role == User.Role.ADMIN ? Disease.Status.APPROVED : Disease.Status.PENDING);
        if (disease.getStatus() == Disease.Status.APPROVED) {
            disease.setPublishedAt(LocalDateTime.now());
        }
        return diseaseRepository.save(disease);
    }

    @Transactional
    public Disease updateDisease(Long id, Disease updates) {
        Disease existing = diseaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("疾病不存在"));
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getAlias() != null) existing.setAlias(updates.getAlias());
        if (updates.getCause() != null) existing.setCause(updates.getCause());
        if (updates.getSymptoms() != null) existing.setSymptoms(updates.getSymptoms());
        if (updates.getTreatment() != null) existing.setTreatment(updates.getTreatment());
        if (updates.getPrevention() != null) existing.setPrevention(updates.getPrevention());
        if (updates.getClassification() != null) existing.setClassification(updates.getClassification());
        existing.setStatus(Disease.Status.PENDING);
        return diseaseRepository.save(existing);
    }

    @Transactional
    public void deleteDisease(Long id) {
        diseaseRepository.deleteById(id);
    }

    public List<Disease> getTopDiseases(int limit) {
        return diseaseRepository.findTopByViews(PageRequest.of(0, limit));
    }
}
