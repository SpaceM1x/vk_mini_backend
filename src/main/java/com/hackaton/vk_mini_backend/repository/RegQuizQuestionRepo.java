package com.hackaton.vk_mini_backend.repository;

import com.hackaton.vk_mini_backend.model.RegQuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegQuizQuestionRepo extends JpaRepository<RegQuizQuestion, Long> {
    List<RegQuizQuestion> findAllByQuizId(Long quizId);
}
