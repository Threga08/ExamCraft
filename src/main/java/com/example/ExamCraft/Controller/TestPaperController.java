package com.example.ExamCraft.Controller;

import com.example.ExamCraft.model.GenerateTestPaperRequest;
import com.example.ExamCraft.model.TestPaper;
import com.example.ExamCraft.service.TestPaperService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-papers")
public class TestPaperController {
	private final TestPaperService testPaperService;

	public TestPaperController(TestPaperService testPaperService) { this.testPaperService = testPaperService; }

	@PostMapping("/generate")
	public ResponseEntity<TestPaper> generate(@Valid @RequestBody GenerateTestPaperRequest request) {
		TestPaper saved = testPaperService.generateRandomizedTestPaper(request);
		return ResponseEntity.created(URI.create("/api/test-papers/" + saved.getId())).body(saved);
	}

	@GetMapping
	public List<TestPaper> getAll() { return testPaperService.getAllTestPapers(); }

	@GetMapping("/{id}")
	public TestPaper getById(@PathVariable Long id) { return testPaperService.getTestPaperById(id); }
}
