package com.example.ExamCraft.service;
import java.util.List;
import java.util.Map;
import com.example.ExamCraft.Repository.AttemptRepository;
import com.example.ExamCraft.Repository.TestPaperRepository;
import com.example.ExamCraft.dto.AttemptSubmissionRequest;
import com.example.ExamCraft.exception.InvalidRequestException;
import com.example.ExamCraft.exception.ResourceNotFoundException;
import com.example.ExamCraft.model.Attempt;
import com.example.ExamCraft.model.Question;
import com.example.ExamCraft.model.TestPaper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AttemptService {
    private final AttemptRepository attemptRepository;
    private final TestPaperRepository testPaperRepository;

    public AttemptService(AttemptRepository attemptRepository, TestPaperRepository testPaperRepository) {
        this.attemptRepository = attemptRepository;
        this.testPaperRepository = testPaperRepository;
    }
    public Attempt saveAttempt(Attempt attempt) {
        return attemptRepository.save(attempt);
    }
    public Attempt getAttemptById(Long id) {
        return attemptRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + id));
    }
    public List<Attempt> getAllAttempts() {
        return attemptRepository.findAll();
    }
    public void deleteAttempt(Long id) {
        attemptRepository.deleteById(id);
    }
    public Attempt updateAttempt(Long id, Attempt updatedAttempt) {
        Attempt existingAttempt = getAttemptById(id);
        existingAttempt.setStudentName(updatedAttempt.getStudentName());
        existingAttempt.setTestPaper(updatedAttempt.getTestPaper());
        existingAttempt.setScore(updatedAttempt.getScore());
        return attemptRepository.save(existingAttempt);
    }

    @Transactional
    public Attempt gradeAttempt(AttemptSubmissionRequest request) {
        if (request == null || request.getStudentName() == null || request.getStudentName().isBlank()
                || request.getTestPaperId() == null || request.getAnswers() == null) {
            throw new InvalidRequestException("Student name, test paper, and answers are required.");
        }
        TestPaper paper = testPaperRepository.findById(request.getTestPaperId())
                .orElseThrow(() -> new ResourceNotFoundException("Test paper not found: " + request.getTestPaperId()));
        Map<Long, String> answers = request.getAnswers();
        for (Map.Entry<Long, String> answer : answers.entrySet()) {
            boolean belongsToPaper = paper.getQuestions().stream()
                    .anyMatch(question -> question.getId().equals(answer.getKey()));
            if (!belongsToPaper) {
                throw new InvalidRequestException("Answer refers to a question not included in this test paper.");
            }
            if (answer.getValue() == null || !answer.getValue().trim().matches("(?i)[ABCD]")) {
                throw new InvalidRequestException("Each answer must be A, B, C, or D.");
            }
        }
        int correct = 0;
        for (Question question : paper.getQuestions()) {
            String submitted = answers.get(question.getId());
            if (submitted != null && submitted.trim().equalsIgnoreCase(question.getCorrectAnswer())) {
                correct++;
            }
        }
        Attempt attempt = new Attempt();
        attempt.setStudentName(request.getStudentName());
        attempt.setTestPaper(paper);
        attempt.setTotalQuestions(paper.getQuestions().size());
        attempt.setCorrectAnswers(correct);
        attempt.setWrongAnswers(paper.getQuestions().size() - correct);
        attempt.setScore(correct);
        return attemptRepository.save(attempt);
    }
}