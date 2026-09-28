package com.example.ExamCraft;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Map;
import com.example.ExamCraft.Repository.AttemptRepository;
import com.example.ExamCraft.Repository.QuestionRepository;
import com.example.ExamCraft.Repository.TestPaperRepository;
import com.example.ExamCraft.Repository.UnitRepository;
import com.example.ExamCraft.dto.AttemptSubmissionRequest;
import com.example.ExamCraft.exception.DuplicateSelectionException;
import com.example.ExamCraft.exception.InsufficientQuestionsException;
import com.example.ExamCraft.model.Difficulty;
import com.example.ExamCraft.model.GenerateTestPaperRequest;
import com.example.ExamCraft.model.Question;
import com.example.ExamCraft.model.TestPaper;
import com.example.ExamCraft.model.Unit;
import com.example.ExamCraft.service.AttemptService;
import com.example.ExamCraft.service.QuestionService;
import com.example.ExamCraft.service.TestPaperService;
import com.example.ExamCraft.service.UnitService;

@SpringBootTest
@Transactional
class ExamCraftApplicationTests {
	@Autowired private AttemptRepository attemptRepository;
	@Autowired private TestPaperRepository testPaperRepository;
	@Autowired private QuestionRepository questionRepository;
	@Autowired private UnitRepository unitRepository;
	@Autowired private AttemptService attemptService;
	@Autowired private QuestionService questionService;
	@Autowired private TestPaperService testPaperService;
	@Autowired private UnitService unitService;

	@Test
	void contextLoads() {
	}

	@Test
	void generatesUniqueDistributedPaperAndGradesAttempt() {
		attemptRepository.deleteAll();
		testPaperRepository.deleteAll();
		questionRepository.deleteAll();
		unitRepository.deleteAll();

		Unit unit = unitService.saveUnit(new Unit("Algebra"));
		Question easy = questionService.saveQuestion(question("Easy question", Difficulty.EASY, unit));
		questionService.saveQuestion(question("Medium question", Difficulty.MEDIUM, unit));
		questionService.saveQuestion(question("Hard question", Difficulty.HARD, unit));

		GenerateTestPaperRequest request = new GenerateTestPaperRequest();
		request.setTotalQuestions(3);
		request.setEasyPercent(34);
		request.setMediumPercent(33);
		request.setHardPercent(33);
		TestPaper paper = testPaperService.generateRandomizedTestPaper(request);

		assertEquals(3, paper.getQuestions().size());
		assertEquals(3, paper.getQuestions().stream().map(Question::getId).distinct().count());
		assertEquals(1, paper.getQuestions().stream().filter(q -> q.getDifficulty() == Difficulty.EASY).count());
		assertEquals(1, paper.getQuestions().stream().filter(q -> q.getDifficulty() == Difficulty.MEDIUM).count());
		assertEquals(1, paper.getQuestions().stream().filter(q -> q.getDifficulty() == Difficulty.HARD).count());
		assertEquals(1, testPaperRepository.countUsageByQuestionId(easy.getId()));

		AttemptSubmissionRequest submission = new AttemptSubmissionRequest();
		submission.setStudentName("Test Student");
		submission.setTestPaperId(paper.getId());
		submission.setAnswers(paper.getQuestions().stream()
				.collect(java.util.stream.Collectors.toMap(Question::getId, question -> "A")));
		var result = attemptService.gradeAttempt(submission);
		assertEquals(3, result.getTotalQuestions());
		assertEquals(3, result.getCorrectAnswers());
		assertEquals(0, result.getWrongAnswers());

		GenerateTestPaperRequest insufficient = new GenerateTestPaperRequest();
		insufficient.setTotalQuestions(2);
		insufficient.setEasyPercent(100);
		insufficient.setMediumPercent(0);
		insufficient.setHardPercent(0);
		assertThrows(InsufficientQuestionsException.class,
				() -> testPaperService.generateRandomizedTestPaper(insufficient));

		TestPaper duplicatePaper = new TestPaper();
		duplicatePaper.setQuestions(List.of(easy, easy));
		assertThrows(DuplicateSelectionException.class, () -> testPaperService.saveTestPaper(duplicatePaper));
		assertTrue(attemptRepository.findAll().stream().anyMatch(attempt -> attempt.getId().equals(result.getId())));
	}

	private Question question(String text, Difficulty difficulty, Unit unit) {
		return questionService.saveQuestion(new Question(text, "A", "B", "C", "D", "A", "Algebra", difficulty, unit));
	}

}
