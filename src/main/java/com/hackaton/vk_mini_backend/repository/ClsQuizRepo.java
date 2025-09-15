package com.hackaton.vk_mini_backend.repository;

import com.hackaton.vk_mini_backend.model.ClsQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClsQuizRepo extends JpaRepository<ClsQuiz, Long> {
    Optional<ClsQuiz> findByLessonId(Long lessonId);
}
