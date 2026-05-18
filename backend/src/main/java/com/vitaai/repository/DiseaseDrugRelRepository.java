package com.vitaai.repository;

import com.vitaai.entity.DiseaseDrugRel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiseaseDrugRelRepository extends JpaRepository<DiseaseDrugRel, DiseaseDrugRel.DiseaseDrugRelId> {
    List<DiseaseDrugRel> findByDiseaseId(Long diseaseId);
    List<DiseaseDrugRel> findByDrugId(Long drugId);
    void deleteByDiseaseIdAndDrugId(Long diseaseId, Long drugId);
}
