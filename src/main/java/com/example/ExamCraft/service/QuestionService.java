package com.example.ExamCraft.service;
import java.util.List;
import com.example.ExamCraft.model.Question;
import com.example.ExamCraft.Repository.QuestionRepository;
import com.example.ExamCraft.Repository.TestPaperRepository;
import com.example.ExamCraft.Repository.UnitRepository;
import com.example.ExamCraft.exception.InvalidRequestException;
import com.example.ExamCraft.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;   

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final TestPaperRepository testPaperRepository;
    private final UnitRepository unitRepository;

    public QuestionService(QuestionRepository questionRepository, TestPaperRepository testPaperRepository,
            UnitRepository unitRepository) {
        this.questionRepository = questionRepository;
        this.testPaperRepository = testPaperRepository;
        this.unitRepository = unitRepository;
    }

    public Question saveQuestion(Question question) {
        question.setUnit(resolveUnit(question.getUnit()));
        validateCorrectAnswer(question);
        return questionRepository.save(question);
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + id));
    }

    public List<Question> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        questions.forEach(question -> question.setUsageCount(testPaperRepository.countUsageByQuestionId(question.getId())));
        return questions;
    }

    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found: " + id);
        }
        questionRepository.deleteById(id);
    }

    public Question updateQuestion(Long id, Question updatedQuestion) {
        Question existingQuestion = getQuestionById(id);
        existingQuestion.setQuestionText(updatedQuestion.getQuestionText());
        existingQuestion.setOptionA(updatedQuestion.getOptionA());
        existingQuestion.setOptionB(updatedQuestion.getOptionB());
        existingQuestion.setOptionC(updatedQuestion.getOptionC());
        existingQuestion.setOptionD(updatedQuestion.getOptionD());
        existingQuestion.setCorrectAnswer(updatedQuestion.getCorrectAnswer());
        existingQuestion.setTopic(updatedQuestion.getTopic());
        existingQuestion.setDifficulty(updatedQuestion.getDifficulty());
        existingQuestion.setUnit(resolveUnit(updatedQuestion.getUnit()));
        validateCorrectAnswer(existingQuestion);
        return questionRepository.save(existingQuestion);
    }

    private com.example.ExamCraft.model.Unit resolveUnit(com.example.ExamCraft.model.Unit unit) {
        if (unit == null || unit.getId() == null) {
            throw new InvalidRequestException("A valid unit is required for each question.");
        }
        return unitRepository.findById(unit.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unit.getId()));
    }

    private void validateCorrectAnswer(Question question) {
        String answer = question.getCorrectAnswer();
        if (answer == null || !answer.trim().toUpperCase().matches("[ABCD]")) {
            throw new IllegalArgumentException("correctAnswer must be A, B, C, or D.");
        }
        question.setCorrectAnswer(answer.trim().toUpperCase());
    }
}