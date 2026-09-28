package com.example.ExamCraft.Repository;
import com.example.ExamCraft.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
}