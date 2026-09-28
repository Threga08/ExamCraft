package com.example.ExamCraft.service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ExamCraft.exception.DuplicateSelectionException;
import com.example.ExamCraft.exception.InsufficientQuestionsException;
import com.example.ExamCraft.exception.InvalidRequestException;
import com.example.ExamCraft.exception.ResourceNotFoundException;
import com.example.ExamCraft.model.Difficulty;
import com.example.ExamCraft.model.GenerateTestPaperRequest;
import com.example.ExamCraft.model.Question;
import com.example.ExamCraft.model.TestPaper;
import com.example.ExamCraft.Repository.QuestionRepository;
import com.example.ExamCraft.Repository.TestPaperRepository;

@Service
public class TestPaperService {
    private final TestPaperRepository testPaperRepository;
    private final QuestionRepository questionRepository;

    public TestPaperService(TestPaperRepository testPaperRepository, QuestionRepository questionRepository) {
        this.testPaperRepository = testPaperRepository;
        this.questionRepository = questionRepository;
    }

    public TestPaper saveTestPaper(TestPaper testPaper) {
        validateUniqueQuestions(testPaper);
        return testPaperRepository.save(testPaper);
    }

    public TestPaper getTestPaperById(Long id) {
        return testPaperRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Test paper not found: " + id));
    }

    public List<TestPaper> getAllTestPapers() {
        return testPaperRepository.findAll();
    }

    public void deleteTestPaper(Long id) {
        if (!testPaperRepository.existsById(id)) {
            throw new ResourceNotFoundException("Test paper not found: " + id);
        }
        testPaperRepository.deleteById(id);
    }

    public TestPaper updateTestPaper(Long id, TestPaper updatedTestPaper) {
        TestPaper existingTestPaper = getTestPaperById(id);
        existingTestPaper.setTestPaperName(updatedTestPaper.getTestPaperName());
        existingTestPaper.setQuestions(updatedTestPaper.getQuestions());
        validateUniqueQuestions(existingTestPaper);
        return testPaperRepository.save(existingTestPaper);
    }
    private void validateUniqueQuestions(TestPaper testPaper) {
        if (testPaper == null || testPaper.getQuestions() == null) {
            throw new InvalidRequestException("A test paper must include a question list.");
        }
        Set<Long> questionIds = new HashSet<>();
        for (Question question : testPaper.getQuestions()) {
            if (question == null || question.getId() == null || !questionIds.add(question.getId())) {
                throw new DuplicateSelectionException("A test paper cannot contain duplicate or unsaved questions.");
            }
        }
    }

    @Transactional
    public TestPaper generateRandomizedTestPaper(GenerateTestPaperRequest request) {
        if (request == null || request.getTotalQuestions() == null || request.getTotalQuestions() <= 0
                || request.getEasyPercent() == null || request.getMediumPercent() == null
                || request.getHardPercent() == null) {
            throw new InvalidRequestException("Total questions and all difficulty percentages are required.");
        }
        if (request.getEasyPercent() < 0 || request.getEasyPercent() > 100
                || request.getMediumPercent() < 0 || request.getMediumPercent() > 100
                || request.getHardPercent() < 0 || request.getHardPercent() > 100) {
            throw new InvalidRequestException("Difficulty percentages must be between 0 and 100.");
        }
        if (request.getEasyPercent() + request.getMediumPercent() + request.getHardPercent() != 100) {
            throw new InvalidRequestException("Difficulty percentages must add up to 100.");
        }

        Map<Difficulty, Integer> percentages = new EnumMap<>(Difficulty.class);
        percentages.put(Difficulty.EASY, request.getEasyPercent());
        percentages.put(Difficulty.MEDIUM, request.getMediumPercent());
        percentages.put(Difficulty.HARD, request.getHardPercent());
        Map<Difficulty, Integer> counts = calculateCounts(request.getTotalQuestions(), percentages);
        List<Question> selected = new ArrayList<>();

        for (Difficulty difficulty : Difficulty.values()) {
            List<Question> available = questionRepository.findByDifficulty(difficulty);
            int required = counts.get(difficulty);
            if (available.size() < required) {
                throw new InsufficientQuestionsException("Not enough " + difficulty + " questions available to satisfy the requested distribution.");
            }
            Collections.shuffle(available);
            selected.addAll(available.subList(0, required));
        }

        TestPaper paper = new TestPaper(request.getTestPaperName() == null || request.getTestPaperName().isBlank()
                ? "Generated Test Paper" : request.getTestPaperName());
        paper.setQuestions(selected);
        return testPaperRepository.save(paper);
    }

    private Map<Difficulty, Integer> calculateCounts(int total, Map<Difficulty, Integer> percentages) {
        Map<Difficulty, Integer> counts = new EnumMap<>(Difficulty.class);
        Map<Difficulty, Double> remainders = new EnumMap<>(Difficulty.class);
        int assigned = 0;
        for (Difficulty difficulty : Difficulty.values()) {
            double exact = total * percentages.get(difficulty) / 100.0;
            int count = (int) Math.floor(exact);
            counts.put(difficulty, count);
            remainders.put(difficulty, exact - count);
            assigned += count;
        }
        while (assigned < total) {
            Difficulty next = Difficulty.EASY;
            for (Difficulty difficulty : Difficulty.values()) {
                if (remainders.get(difficulty) > remainders.get(next)) next = difficulty;
            }
            counts.put(next, counts.get(next) + 1);
            remainders.put(next, -1.0);
            assigned++;
        }
        return counts;
    }
}