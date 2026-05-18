package com.vitaai.service;

import com.vitaai.entity.Drug;
import com.vitaai.entity.User;
import com.vitaai.repository.DrugRepository;
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
public class DrugService {

    private final DrugRepository drugRepository;
    private final UserRepository userRepository;

    public Page<Drug> getDrugs(int page, int pageSize, String keyword, String drugType) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "viewsCount"));
        if (keyword != null && !keyword.isEmpty()) {
            return drugRepository.search(keyword, pageable);
        }
        if (drugType != null && !drugType.isEmpty()) {
            return drugRepository.findByDrugTypeAndStatus(Drug.DrugType.valueOf(drugType), Drug.Status.APPROVED, pageable);
        }
        return drugRepository.findAllApproved(pageable);
    }

    public Drug getDrug(Long id) {
        Drug drug = drugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("药品不存在"));
        Integer current = drug.getViewsCount();
        drug.setViewsCount((current == null ? 0 : current) + 1);
        drugRepository.save(drug);
        return drug;
    }

    @Transactional
    public Drug createDrug(Drug drug, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        drug.setCreatedBy(user);
        drug.setStatus(user.getRole() == User.Role.ADMIN ? Drug.Status.APPROVED : Drug.Status.PENDING);
        if (drug.getStatus() == Drug.Status.APPROVED) {
            drug.setPublishedAt(LocalDateTime.now());
        }
        return drugRepository.save(drug);
    }

    @Transactional
    public Drug updateDrug(Long id, Drug updates) {
        Drug existing = drugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("药品不存在"));
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getGenericName() != null) existing.setGenericName(updates.getGenericName());
        if (updates.getEfficacy() != null) existing.setEfficacy(updates.getEfficacy());
        if (updates.getUsage2() != null) existing.setUsage2(updates.getUsage2());
        if (updates.getDosage() != null) existing.setDosage(updates.getDosage());
        if (updates.getSideEffect() != null) existing.setSideEffect(updates.getSideEffect());
        if (updates.getContraindication() != null) existing.setContraindication(updates.getContraindication());
        existing.setStatus(Drug.Status.PENDING);
        return drugRepository.save(existing);
    }

    @Transactional
    public void deleteDrug(Long id) {
        drugRepository.deleteById(id);
    }

    public List<Drug> getTopDrugs(int limit) {
        return drugRepository.findTopByViews(PageRequest.of(0, limit));
    }
}
