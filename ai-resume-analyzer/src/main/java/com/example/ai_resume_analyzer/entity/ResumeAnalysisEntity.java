
package com.example.ai_resume_analyzer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.example.ai_resume_analyzer.entity.AppUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "resume_analyses")
public class ResumeAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @NotBlank(message = "Professional summary cannot be empty")
    @Size(max = 5000, message = "Professional summary cannot exceed 5000 characters")
    @Column(columnDefinition = "TEXT")
    private String professionalSummary;

    @NotBlank(message = "Technical skills cannot be empty")
    @Size(max = 5000, message = "Technical skills cannot exceed 5000 characters")
    @Column(columnDefinition = "TEXT")
    private String technicalSkills;


    @NotBlank(message = "Projects and experience cannot be empty")
    @Size(max = 5000, message = "Projects and experience cannot exceed 5000 characters")
    @Column(columnDefinition = "TEXT")
    private String projectsAndExperience;

    @NotBlank(message = "Strengths cannot be empty")
    @Size(max = 5000, message = "Strengths cannot exceed 5000 characters")
    @Column(columnDefinition = "TEXT")
    private String strengths;

    @NotBlank(message = "Areas to improve cannot be empty")
    @Size(max = 5000, message = "Areas to improve cannot exceed 5000 characters")
    @Column(columnDefinition = "TEXT")
    private String areasToImprove;

    public ResumeAnalysisEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getProfessionalSummary() {
        return professionalSummary;
    }

    public void setProfessionalSummary(String professionalSummary) {
        this.professionalSummary = professionalSummary;
    }

    public String getTechnicalSkills() {
        return technicalSkills;
    }

    public void setTechnicalSkills(String technicalSkills) {
        this.technicalSkills = technicalSkills;
    }

    public String getProjectsAndExperience() {
        return projectsAndExperience;
    }

    public void setProjectsAndExperience(String projectsAndExperience) {
        this.projectsAndExperience = projectsAndExperience;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getAreasToImprove() {
        return areasToImprove;
    }

    public void setAreasToImprove(String areasToImprove) {
        this.areasToImprove = areasToImprove;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

}
