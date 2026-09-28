package com.example.ExamCraft.Controller;

import com.example.ExamCraft.model.Question;
import com.example.ExamCraft.service.QuestionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
	private final QuestionService questionService;

	public QuestionController(QuestionService questionService) {
		this.questionService = questionService;
	}

	@PostMapping
	public ResponseEntity<Question> create(@Valid @RequestBody Question question) {
		Question saved = questionService.saveQuestion(question);
		return ResponseEntity.created(URI.create("/api/questions/" + saved.getId())).body(saved);
	}

	@GetMapping
	public List<Question> getAll() { return questionService.getAllQuestions(); }

	@GetMapping("/{id}")
	public Question getById(@PathVariable Long id) { return questionService.getQuestionById(id); }

	@PutMapping("/{id}")
	public Question update(@PathVariable Long id, @Valid @RequestBody Question question) {
		return questionService.updateQuestion(id, question);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		questionService.deleteQuestion(id);
		return ResponseEntity.noContent().build();
	}
}