
package com.example.ai_resume_analyzer.service;

import com.example.ai_resume_analyzer.entity.ResumeAnalysisEntity;
import com.example.ai_resume_analyzer.repository.ResumeAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.ai_resume_analyzer.repository.AppUserRepository;
import com.example.ai_resume_analyzer.entity.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ResumeAnalysisPersistenceService {

    private final ResumeAnalysisRepository repository;
    private final AppUserRepository userRepository;

    public ResumeAnalysisPersistenceService(
            ResumeAnalysisRepository repository,
            AppUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }
    private AppUser getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    public ResumeAnalysisEntity save(
            ResumeAnalysisEntity analysis, Long userId) {
        analysis.setUser(getUser(userId));
        return repository.save(analysis);
    }

    public Page<ResumeAnalysisEntity> findAll(
            Long userId, Pageable pageable) {

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id")
        );

        return repository.findAllByUserId(userId, sortedPageable);
    }


    public ResumeAnalysisEntity findById(Long id, Long userId) {

        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resume analysis not found"));
    }


    public Page<ResumeAnalysisEntity> searchBySkill(
            String skill, Long userId, Pageable pageable) {

        return repository
                .findByTechnicalSkillsContainingIgnoreCaseAndUserId(
                        skill, userId, pageable);
    }


    public void deleteById(Long id, Long userId) {

        if (!repository.existsByIdAndUserId(id, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Resume analysis not found");
        }

        repository.deleteByIdAndUserId(id, userId);
    }


    public ResumeAnalysisEntity update(
            Long id, Long userId,ResumeAnalysisEntity updatedAnalysis) {

        ResumeAnalysisEntity existing = repository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Resume analysis not found"));

        existing.setProfessionalSummary(updatedAnalysis.getProfessionalSummary());
        existing.setTechnicalSkills(updatedAnalysis.getTechnicalSkills());
        existing.setProjectsAndExperience(updatedAnalysis.getProjectsAndExperience());
        existing.setStrengths(updatedAnalysis.getStrengths());
        existing.setAreasToImprove(updatedAnalysis.getAreasToImprove());

        return repository.save(existing);
    }


}
