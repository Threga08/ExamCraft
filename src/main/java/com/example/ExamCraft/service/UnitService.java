package com.example.ExamCraft.service;
import java.util.List;
import com.example.ExamCraft.model.Unit;
import com.example.ExamCraft.Repository.UnitRepository;
import com.example.ExamCraft.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UnitService {
    private final UnitRepository unitRepository;

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }
    public Unit saveUnit(Unit unit) {
        return unitRepository.save(unit);
    }
    public Unit getUnitById(Long id) {
        return unitRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + id));
    }
    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }
    public void deleteUnit(Long id) {
        if (!unitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Unit not found: " + id);
        }
        unitRepository.deleteById(id);
    }
    public Unit updateUnit(Long id, Unit updatedUnit) {
        Unit existingUnit = getUnitById(id);
        existingUnit.setUnitName(updatedUnit.getUnitName());
        return unitRepository.save(existingUnit);
    } 

}