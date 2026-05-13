package com.vitaai.repository;

import com.vitaai.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByIsActiveTrue();

    List<Skill> findByCategoryAndIsActiveTrue(String category);

    Page<Skill> findAll(Pageable pageable);

    Page<Skill> findByCategory(String category, Pageable pageable);
}
