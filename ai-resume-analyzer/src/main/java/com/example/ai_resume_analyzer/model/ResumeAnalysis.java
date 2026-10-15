package com.example.ai_resume_analyzer.model;

import java.util.List;

public class ResumeAnalysis {

    private String professionalSummary;
    private List<String> technicalSkills;
    private List<String> projectsAndExperience;
    private List<String> strengths;
    private List<String> areasToImprove;

    public String getProfessionalSummary() {
        return professionalSummary;
    }

    public void setProfessionalSummary(String professionalSummary) {
        this.professionalSummary = professionalSummary;
    }

    public List<String> getTechnicalSkills() {
        return technicalSkills;
    }

    public void setTechnicalSkills(List<String> technicalSkills) {
        this.technicalSkills = technicalSkills;
    }

    public List<String> getProjectsAndExperience() {
        return projectsAndExperience;
    }

    public void setProjectsAndExperience(List<String> projectsAndExperience) {
        this.projectsAndExperience = projectsAndExperience;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getAreasToImprove() {
        return areasToImprove;
    }

    public void setAreasToImprove(List<String> areasToImprove) {
        this.areasToImprove = areasToImprove;
    }
}