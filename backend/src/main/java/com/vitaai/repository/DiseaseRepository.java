package com.vitaai.repository;

import com.vitaai.entity.Disease;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    Page<Disease> findByStatus(Disease.Status status, Pageable pageable);

    @Query("SELECT d FROM Disease d WHERE d.status = 'APPROVED' AND " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.symptoms) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.classification) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Disease> search(String keyword, Pageable pageable);

    @Query("SELECT d FROM Disease d WHERE d.status = 'APPROVED'")
    Page<Disease> findAllApproved(Pageable pageable);

    Page<Disease> findByClassificationAndStatus(String classification, Disease.Status status, Pageable pageable);

    @Query("SELECT d FROM Disease d WHERE d.status = 'APPROVED' ORDER BY d.viewsCount DESC")
    List<Disease> findTopByViews(Pageable pageable);

    List<Disease> findByCreatedById(Long userId);

    Page<Disease> findByStatusIn(List<Disease.Status> statuses, Pageable pageable);
}
