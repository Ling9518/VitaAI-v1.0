package com.vitaai.repository;

import com.vitaai.entity.DiseaseSkillRel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiseaseSkillRelRepository extends JpaRepository<DiseaseSkillRel, DiseaseSkillRel.DiseaseSkillRelId> {
    List<DiseaseSkillRel> findByDiseaseId(Long diseaseId);
    List<DiseaseSkillRel> findBySkillId(Long skillId);
}
