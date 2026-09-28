package com.example.ExamCraft.Controller;

import com.example.ExamCraft.dto.AttemptSubmissionRequest;
import com.example.ExamCraft.model.Attempt;
import com.example.ExamCraft.service.AttemptService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {
    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) { this.attemptService = attemptService; }

    @PostMapping
    public ResponseEntity<Attempt> submit(@Valid @RequestBody AttemptSubmissionRequest request) {
        Attempt saved = attemptService.gradeAttempt(request);
        return ResponseEntity.created(URI.create("/api/attempts/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public Attempt getById(@PathVariable Long id) { return attemptService.getAttemptById(id); }
}