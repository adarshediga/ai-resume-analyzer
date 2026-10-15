
package com.example.ai_resume_analyzer.model;

public class InterviewAnswerRequest {

    private String question;
    private String jobDescription;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
