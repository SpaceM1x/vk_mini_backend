package com.hackaton.vk_mini_backend.controller;

import com.hackaton.vk_mini_backend.model.ClsQuiz;
import com.hackaton.vk_mini_backend.service.ClsQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Контроллер тестов", description = "Действия с таблицей тестов")
public class ClsQuizController {
    private final ClsQuizService clsQuizService;

    @GetMapping("/by-lesson/{lessonId}")
    @Operation(summary = "Получить тест со всеми вопросами и вариантами ответов по ID урока")
    public ResponseEntity<ClsQuiz> findByLessonId(@Parameter(description = "ID урока") @PathVariable Long lessonId) {
        ResponseEntity<ClsQuiz> responseEntity =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        try {
            ClsQuiz quiz = clsQuizService.findByLessonId(lessonId);
            responseEntity = ResponseEntity.ok(quiz);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return responseEntity;
    }
}
