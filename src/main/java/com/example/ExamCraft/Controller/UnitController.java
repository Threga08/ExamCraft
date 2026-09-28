package com.example.ExamCraft.Controller;

import com.example.ExamCraft.model.Unit;
import com.example.ExamCraft.service.UnitService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/units")
public class UnitController {
	private final UnitService unitService;

	public UnitController(UnitService unitService) { this.unitService = unitService; }

	@PostMapping
	public ResponseEntity<Unit> create(@Valid @RequestBody Unit unit) {
		Unit saved = unitService.saveUnit(unit);
		return ResponseEntity.created(URI.create("/api/units/" + saved.getId())).body(saved);
	}

	@GetMapping
	public List<Unit> getAll() { return unitService.getAllUnits(); }

	@GetMapping("/{id}")
	public Unit getById(@PathVariable Long id) { return unitService.getUnitById(id); }

	@PutMapping("/{id}")
	public Unit update(@PathVariable Long id, @Valid @RequestBody Unit unit) {
		return unitService.updateUnit(id, unit);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		unitService.deleteUnit(id);
		return ResponseEntity.noContent().build();
	}
}
