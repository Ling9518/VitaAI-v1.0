package com.vitaai.repository;

import com.vitaai.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugRepository extends JpaRepository<Drug, Long> {
    Page<Drug> findByStatus(Drug.Status status, Pageable pageable);

    @Query("SELECT d FROM Drug d WHERE d.status = 'APPROVED' AND " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.genericName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.efficacy) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Drug> search(String keyword, Pageable pageable);

    @Query("SELECT d FROM Drug d WHERE d.status = 'APPROVED'")
    Page<Drug> findAllApproved(Pageable pageable);

    @Query("SELECT d FROM Drug d WHERE d.status = 'APPROVED' ORDER BY d.viewsCount DESC")
    List<Drug> findTopByViews(Pageable pageable);

    List<Drug> findByCreatedById(Long userId);
}
