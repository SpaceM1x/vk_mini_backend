package com.hackaton.vk_mini_backend.controller;

import com.hackaton.vk_mini_backend.model.ClsLesson;
import com.hackaton.vk_mini_backend.service.ClsLessonService;
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

import java.util.List;

@RestController
@RequestMapping("/api/lesson")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Контроллер уроков", description = "Действия с таблицей уроков")
public class ClsLessonController {

    private final ClsLessonService clsLessonService;

    @GetMapping("/by-course/{courseId}")
    @Operation(summary = "Получить список уроков по ID курса")
    public ResponseEntity<List<ClsLesson>> findByCourseId(
            @Parameter(description = "ID курса") @PathVariable Long courseId) {
        ResponseEntity<List<ClsLesson>> responseEntity =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        try {
            List<ClsLesson> lessons = clsLessonService.findByCourseId(courseId);
            responseEntity = ResponseEntity.ok(lessons);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return responseEntity;
    }
}
