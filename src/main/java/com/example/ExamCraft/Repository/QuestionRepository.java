package com.example.ExamCraft.Repository;

import com.example.ExamCraft.model.Difficulty;
import com.example.ExamCraft.model.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
	List<Question> findByDifficulty(Difficulty difficulty);
	List<Question> findByTopicIgnoreCase(String topic);
	List<Question> findByUnitId(Long unitId);
}