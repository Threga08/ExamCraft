package com.example.ExamCraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class AttemptSubmissionRequest {
    @NotBlank
    private String studentName;
    @NotNull
    private Long testPaperId;
    @NotNull
    private Map<Long, String> answers;

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getTestPaperId() { return testPaperId; }
    public void setTestPaperId(Long testPaperId) { this.testPaperId = testPaperId; }
    public Map<Long, String> getAnswers() { return answers; }
    public void setAnswers(Map<Long, String> answers) { this.answers = answers; }
}
