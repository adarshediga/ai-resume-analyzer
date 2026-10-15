package com.example.ai_resume_analyzer.controller;

import com.example.ai_resume_analyzer.entity.ResumeAnalysisEntity;
import com.example.ai_resume_analyzer.model.InterviewAnswerRequest;
import com.example.ai_resume_analyzer.model.InterviewAnswerResponse;
import com.example.ai_resume_analyzer.model.InterviewQuestionResponse;
import com.example.ai_resume_analyzer.model.JobMatchAnalysis;
import com.example.ai_resume_analyzer.model.ResumeAnalysis;
import com.example.ai_resume_analyzer.repository.AppUserRepository;
import com.example.ai_resume_analyzer.service.ResumeAnalysisPersistenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/resume")
@Tag(
        name = "Resume Analyzer",
        description = "APIs for resume analysis, job matching, interview preparation, and saved resume management"
)
public class ResumeController {

    private final ChatClient chatClient;
    private final ResumeAnalysisPersistenceService persistenceService;
    private final AppUserRepository userRepository;

    public ResumeController(
            ChatClient.Builder builder,
            ResumeAnalysisPersistenceService persistenceService,
            AppUserRepository userRepository) {
        this.chatClient = builder.build();
        this.persistenceService = persistenceService;
        this.userRepository = userRepository;
    }

    /** Extracts text from a real PDF and maps PDF parsing errors to HTTP 400. */
    private String extractResumeText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String resumeText = stripper.getText(document);

            if (resumeText == null || resumeText.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No readable text found in the PDF"
                );
            }

            return resumeText;
        } catch (InvalidPasswordException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password-protected PDFs are not supported"
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or corrupted PDF file"
            );
        }
    }

    private void validatePdfUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please upload a non-empty PDF file"
            );
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF files are allowed"
            );
        }

        // This is an additional check, not proof that the content is a valid PDF.
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF files are allowed"
            );
        }
    }

    private Long authenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"))
                .getId();
    }

    @GetMapping("/saved")
    @Operation(
            summary = "Get saved resumes",
            description = "Returns a paginated list of resumes belonging only to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumes retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    public Page<ResumeAnalysisEntity> getSavedAnalyses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        validatePagination(page, size);
        return persistenceService.findAll(authenticatedUserId(), PageRequest.of(page, size));
    }

    @PutMapping(value = "/saved/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update a saved resume",
            description = "Updates resume analysis fields only when the resume belongs to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid resume data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resume not found for this user")
    })
    public ResumeAnalysisEntity updateSavedAnalysis(
            @PathVariable Long id,
            @Valid @RequestBody ResumeAnalysisEntity updatedAnalysis) {
        return persistenceService.update(id, authenticatedUserId(), updatedAnalysis);
    }

    @GetMapping("/saved/search")
    @Operation(
            summary = "Search resumes by skill",
            description = "Searches saved resumes by technical skill, returning only results belonging to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Skill or pagination parameters are invalid"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    public Page<ResumeAnalysisEntity> searchSavedAnalyses(
            @RequestParam String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        if (skill == null || skill.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skill cannot be empty");
        }
        validatePagination(page, size);
        return persistenceService.searchBySkill(skill.trim(), authenticatedUserId(), PageRequest.of(page, size));
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be between 1 and 100");
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload and analyze a resume PDF",
            description = "Uploads a PDF resume, extracts readable text, generates an AI analysis, and saves it for the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume analyzed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid, empty, password-protected, or unreadable PDF"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "500", description = "Unexpected server or AI error")
    })
    public ResumeAnalysis uploadResume(@RequestPart("file") MultipartFile file) {
        validatePdfUpload(file);
        String resumeText = extractResumeText(file);

        ResumeAnalysis analysis = chatClient
                .prompt()
                .user("""
                        You are an experienced technical recruiter.

                        Analyze the resume provided below.

                        Return these fields:
                        - professionalSummary: summarize the candidate's background in 2-3 sentences.
                        - technicalSkills: list only skills explicitly mentioned in the resume.
                        - projectsAndExperience: summarize the actual projects and work experience.
                        - strengths: identify strengths supported by evidence in the resume.
                        - areasToImprove: provide 3-5 specific, actionable suggestions.

                        For areasToImprove:
                        - Suggest improvements to project descriptions, measurable outcomes, technical details, and clarity where relevant.
                        - If a section is already adequate, suggest a meaningful next step.
                        - Do not invent qualifications, experience, or achievements.
                        - Do not claim a skill is missing if it is present in the resume.

                        Return information that matches the ResumeAnalysis structure.

                        Resume text:
                        """ + resumeText)
                .call()
                .entity(ResumeAnalysis.class);

        ResumeAnalysisEntity entity = new ResumeAnalysisEntity();
        entity.setProfessionalSummary(analysis.getProfessionalSummary());
        entity.setTechnicalSkills(analysis.getTechnicalSkills() == null
                ? "" : String.join(", ", analysis.getTechnicalSkills()));
        entity.setProjectsAndExperience(analysis.getProjectsAndExperience() == null
                ? "" : String.join("\n", analysis.getProjectsAndExperience()));
        entity.setStrengths(analysis.getStrengths() == null
                ? "" : String.join("\n", analysis.getStrengths()));
        entity.setAreasToImprove(analysis.getAreasToImprove() == null
                ? "" : String.join("\n", analysis.getAreasToImprove()));

        persistenceService.save(entity, authenticatedUserId());
        return analysis;
    }

    @GetMapping("/health")
    @Operation(summary = "Check application status", description = "Checks whether the resume controller is responding.")
    @ApiResponse(responseCode = "200", description = "Service is responding")
    public Map<String, String> healthCheck() {
        return Map.of("status", "UP", "application", "AI Resume Analyzer");
    }

    @PostMapping(value = "/match", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Match a resume against a job description",
            description = "Compares resume evidence with job requirements and suggests improvements."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job match analysis completed"),
            @ApiResponse(responseCode = "400", description = "Invalid PDF or empty job description"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public JobMatchAnalysis matchResumeToJob(
            @RequestPart("file") MultipartFile file,
            @RequestPart("jobDescription") String jobDescription) {
        validatePdfUpload(file);
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job description cannot be empty");
        }
        String resumeText = extractResumeText(file);

        return chatClient
                .prompt()
                .user("""
                        You are an experienced technical recruiter.

                        Compare the resume with the job description.

                        Return these fields:
                        - matchPercentage: an integer from 0 to 100, representing an estimated match.
                        - matchingSkills: skills supported by the resume that match the job requirements.
                        - missingSkills: job requirements not evidenced in the resume.
                        - improvementSuggestions: specific ways to improve the resume for this job.

                        Do not invent candidate skills or experience.
                        Do not assume an unmentioned skill is absent; describe it as not evidenced in the resume.
                        Return only information matching the requested response structure.

                        RESUME:
                        """ + resumeText + """

                        JOB DESCRIPTION:
                        """ + jobDescription)
                .call()
                .entity(JobMatchAnalysis.class);
    }

    @PostMapping(value = "/interview-questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Generate interview questions",
            description = "Generates tailored interview questions from the resume and job description."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Questions generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid PDF, job description, or question count"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public InterviewQuestionResponse generateInterviewQuestions(
            @RequestPart("file") MultipartFile file,
            @RequestPart("jobDescription") String jobDescription,
            @RequestParam("numberOfQuestions") int numberOfQuestions) {
        validatePdfUpload(file);
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job description cannot be empty");
        }
        if (numberOfQuestions < 1 || numberOfQuestions > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of questions must be between 1 and 30");
        }
        String resumeText = extractResumeText(file);

        String prompt = """
                You are an experienced technical interviewer.

                Generate exactly %d interview questions using the candidate's resume and the job description.
                Include questions about relevant technical concepts, technologies required by the job,
                the candidate's actual projects and experience, and practical scenario-based problems.
                Tailor the questions to the candidate and role. Do not invent projects or experience.
                Avoid duplicate questions.

                Return a JSON object with exactly this structure:
                {
                  "questions": ["First interview question", "Second interview question"]
                }
                The questions field must be an array of strings, not a bare JSON array.
                Generate exactly %d questions.

                RESUME:
                %s

                JOB DESCRIPTION:
                %s
                """.formatted(numberOfQuestions, numberOfQuestions, resumeText, jobDescription);

        return chatClient.prompt().user(prompt).call().entity(InterviewQuestionResponse.class);
    }

    @PostMapping(value = "/interview-answer", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Evaluate interview answer",
            description = "Evaluates a candidate's answer and provides feedback for improvement."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer evaluated successfully"),
            @ApiResponse(responseCode = "400", description = "Interview question is invalid"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public InterviewAnswerResponse generateInterviewAnswer(@RequestBody InterviewAnswerRequest request) {
        if (request == null || request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interview question cannot be empty");
        }

        String jobDescription = request.getJobDescription();
        if (jobDescription == null || jobDescription.isBlank()) {
            jobDescription = "General Java developer role";
        }

        String prompt = """
                You are an experienced technical interviewer and Java mentor.

                Answer the following interview question.
                Return these fields:
                - answer: a concise, interview-ready answer.
                - explanation: a clear explanation of the concept.
                - example: a practical Java or Spring Boot example where relevant.
                Keep the answer technically accurate. Use code examples when helpful.
                Do not invent the candidate's personal experience.

                Target job description:
                %s

                Interview question:
                %s
                """.formatted(jobDescription, request.getQuestion());

        return chatClient.prompt().user(prompt).call().entity(InterviewAnswerResponse.class);
    }

    @GetMapping("/saved/{id}")
    @Operation(
            summary = "Get resume by ID",
            description = "Retrieves a saved resume only if it belongs to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resume not found for this user")
    })
    public ResumeAnalysisEntity getSavedAnalysisById(@PathVariable Long id) {
        return persistenceService.findById(id, authenticatedUserId());
    }

    @DeleteMapping("/saved/{id}")
    @Operation(
            summary = "Delete a saved resume",
            description = "Deletes a saved resume only if it belongs to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resume deleted successfully if the controller returns 200"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Resume not found for this user")
    })
    public void deleteSavedAnalysis(@PathVariable Long id) {
        persistenceService.deleteById(id, authenticatedUserId());
    }
}
