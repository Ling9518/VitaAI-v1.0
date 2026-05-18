package com.vitaai.repository;

import com.vitaai.entity.SymptomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SymptomCategoryRepository extends JpaRepository<SymptomCategory, Long> {
    List<SymptomCategory> findByParentIsNullAndIsActiveTrueOrderBySortOrder();
    List<SymptomCategory> findByParentIdAndIsActiveTrueOrderBySortOrder(Long parentId);
    List<SymptomCategory> findByIsActiveTrueOrderBySortOrder();
}
