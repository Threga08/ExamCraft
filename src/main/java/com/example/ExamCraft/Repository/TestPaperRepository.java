package com.example.ExamCraft.Repository;

import com.example.ExamCraft.model.TestPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestPaperRepository extends JpaRepository<TestPaper, Long> {
	@Query("select count(tp) from TestPaper tp join tp.questions q where q.id = :questionId")
	long countUsageByQuestionId(@Param("questionId") Long questionId);
}