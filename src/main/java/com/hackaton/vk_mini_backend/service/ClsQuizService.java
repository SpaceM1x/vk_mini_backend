package com.hackaton.vk_mini_backend.service;

import com.hackaton.vk_mini_backend.exception.NoSuchElementFoundException;
import com.hackaton.vk_mini_backend.model.ClsQuiz;
import com.hackaton.vk_mini_backend.repository.ClsQuizRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClsQuizService {
    private final ClsQuizRepo clsQuizRepo;

    public ClsQuiz findByLessonId(Long lessonId) {
        return clsQuizRepo
                .findByLessonId(lessonId)
                .orElseThrow(() -> new NoSuchElementFoundException("Тест не найден"));
    }
}
