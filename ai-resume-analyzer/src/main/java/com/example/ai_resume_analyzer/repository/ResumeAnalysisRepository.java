
package com.example.ai_resume_analyzer.repository;

import com.example.ai_resume_analyzer.entity.ResumeAnalysisEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeAnalysisRepository
        extends JpaRepository<ResumeAnalysisEntity, Long> {

    Page<ResumeAnalysisEntity> findByTechnicalSkillsContainingIgnoreCase(
            String technicalSkills, Pageable pageable);

    Page<ResumeAnalysisEntity> findAllByUserId(
            Long userId, Pageable pageable);

    Optional<ResumeAnalysisEntity> findByIdAndUserId(
            Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    Page<ResumeAnalysisEntity> findByTechnicalSkillsContainingIgnoreCaseAndUserId(
            String technicalSkills, Long userId, Pageable pageable);
}
