package com.hackaton.vk_mini_backend.controller;

import com.hackaton.vk_mini_backend.dto.UserCourseProgressDto;
import com.hackaton.vk_mini_backend.dto.request.UpdateProgressRequestDto;
import com.hackaton.vk_mini_backend.repository.ClsUserRepo;
import com.hackaton.vk_mini_backend.service.UserProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Контроллер прогресса пользователей", description = "Управление прогрессом изучения курсов")
public class UserProgressController {

    private final UserProgressService userProgressService;
    private final ClsUserRepo clsUserRepo;

    @GetMapping("/courses")
    @Operation(summary = "Получить прогресс пользователя по всем курсам")
    public ResponseEntity<List<UserCourseProgressDto>> getUserProgress() {
        try {
            Long userId = getCurrentUserId();
            List<UserCourseProgressDto> progress = userProgressService.getUserProgress(userId);
            return ResponseEntity.ok(progress);
        } catch (Exception ex) {
            log.error("Error getting user progress: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/start/{courseId}")
    @Operation(summary = "Начать изучение курса")
    public ResponseEntity<UserCourseProgressDto> startCourse(@PathVariable Long courseId) {
        try {
            Long userId = getCurrentUserId();
            UserCourseProgressDto progress = userProgressService.startCourse(userId, courseId);
            return ResponseEntity.ok(progress);
        } catch (Exception ex) {
            log.error("Error starting course {}: {}", courseId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update")
    @Operation(summary = "Обновить прогресс изучения курса")
    public ResponseEntity<UserCourseProgressDto> updateProgress(@RequestBody UpdateProgressRequestDto request) {
        try {
            Long userId = getCurrentUserId();
            UserCourseProgressDto progress = userProgressService.updateProgress(userId, request);
            return ResponseEntity.ok(progress);
        } catch (Exception ex) {
            log.error("Error updating progress: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Long getCurrentUserId() {
        String username = ((UserDetails)
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getUsername();
        return clsUserRepo
                .findByLoginIgnoreCaseAndIsDeleted(username, false)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
