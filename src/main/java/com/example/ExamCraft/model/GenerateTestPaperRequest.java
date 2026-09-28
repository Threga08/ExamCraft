package com.example.ExamCraft.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class GenerateTestPaperRequest {
    @NotNull
    @Positive
    private Integer totalQuestions;
    @NotNull @Min(0) @Max(100)
    private Integer easyPercent;
    @NotNull @Min(0) @Max(100)
    private Integer mediumPercent;
    @NotNull @Min(0) @Max(100)
    private Integer hardPercent;
    private String testPaperName;

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getEasyPercent() { return easyPercent; }
    public void setEasyPercent(Integer easyPercent) { this.easyPercent = easyPercent; }
    public Integer getMediumPercent() { return mediumPercent; }
    public void setMediumPercent(Integer mediumPercent) { this.mediumPercent = mediumPercent; }
    public Integer getHardPercent() { return hardPercent; }
    public void setHardPercent(Integer hardPercent) { this.hardPercent = hardPercent; }
    public String getTestPaperName() { return testPaperName; }
    public void setTestPaperName(String testPaperName) { this.testPaperName = testPaperName; }
}
