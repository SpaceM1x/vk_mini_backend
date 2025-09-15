package com.hackaton.vk_mini_backend.service;

import com.hackaton.vk_mini_backend.dto.UserCourseProgressDto;
import com.hackaton.vk_mini_backend.dto.request.UpdateProgressRequestDto;
import com.hackaton.vk_mini_backend.model.*;
import com.hackaton.vk_mini_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProgressService {

    private final RegUserCourseRepo userCourseRepo;
    private final ClsUserRepo userRepo;
    private final RegCourseRepo courseRepo;
    private final ClsCourseStatusRepo courseStatusRepo;

    public List<UserCourseProgressDto> getUserProgress(Long userId) {
        log.info("Getting progress for user: {}", userId);

        List<RegUserCourse> userCourses = userCourseRepo.findStartedCoursesByUserId(userId);

        return userCourses.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public UserCourseProgressDto startCourse(Long userId, Long courseId) {
        log.info("Starting course {} for user {}", courseId, userId);

        // Проверяем, не начат ли уже курс
        var existingProgress = userCourseRepo.findByUserIdAndCourseId(userId, courseId);
        if (existingProgress.isPresent()) {
            log.info("Course {} already started for user {}", courseId, userId);
            return mapToDto(existingProgress.get());
        }

        // Получаем пользователя и курс
        ClsUser user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        RegCourse course =
                courseRepo.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        // Получаем статус "ALIVE"
        ClsCourseStatus aliveStatus = courseStatusRepo
                .findByName("ALIVE")
                .orElseThrow(() -> new RuntimeException("Course status ALIVE not found"));

        // Создаем запись о начале курса
        RegUserCourse userCourse = RegUserCourse.builder()
                .user(user)
                .course(course)
                .status(aliveStatus)
                .enrolled(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        RegUserCourse saved = userCourseRepo.save(userCourse);
        log.info("Course {} started for user {}", courseId, userId);

        return mapToDto(saved);
    }

    @Transactional
    public UserCourseProgressDto updateProgress(Long userId, UpdateProgressRequestDto request) {
        log.info(
                "Updating progress for user {} on course {}: {}%",
                userId, request.getCourseId(), request.getProgress());

        // Находим или создаем запись о прогрессе
        var userCourse = userCourseRepo
                .findByUserIdAndCourseId(userId, request.getCourseId())
                .orElseGet(() -> {
                    // Если записи нет, создаем новую (автоматически начинаем курс)
                    return createNewUserCourse(userId, request.getCourseId());
                });

        // Обновляем прогресс
        if (request.getProgress() != null && request.getProgress() >= 100) {
            // Курс завершен
            userCourse.setCompleted(Timestamp.valueOf(LocalDateTime.now()));
        }

        RegUserCourse saved = userCourseRepo.save(userCourse);
        log.info("Progress updated for user {} on course {}", userId, request.getCourseId());

        return mapToDto(saved);
    }

    private RegUserCourse createNewUserCourse(Long userId, Long courseId) {
        ClsUser user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        RegCourse course =
                courseRepo.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        ClsCourseStatus aliveStatus = courseStatusRepo
                .findByName("ALIVE")
                .orElseThrow(() -> new RuntimeException("Course status ALIVE not found"));

        return RegUserCourse.builder()
                .user(user)
                .course(course)
                .status(aliveStatus)
                .enrolled(Timestamp.valueOf(LocalDateTime.now()))
                .build();
    }

    private UserCourseProgressDto mapToDto(RegUserCourse userCourse) {
        // Вычисляем примерный прогресс (пока простая логика)
        int progress = calculateProgress(userCourse);

        return UserCourseProgressDto.builder()
                .courseId(userCourse.getCourse().getId())
                .courseTitle(userCourse.getCourse().getTitle())
                .courseCategory(
                        userCourse.getCourse().getCourseCategory() != null
                                ? userCourse.getCourse().getCourseCategory().getName()
                                : null)
                .courseDescription(userCourse.getCourse().getDescription())
                .avatarUrl(userCourse.getCourse().getAvatarUrl())
                .progress(progress)
                .score(0) // TODO: вычислять на основе тестов
                .enrolled(userCourse.getEnrolled())
                .completed(userCourse.getCompleted())
                .status(userCourse.getStatus() != null ? userCourse.getStatus().getName() : null)
                .build();
    }

    private int calculateProgress(RegUserCourse userCourse) {
        if (userCourse.getCompleted() != null) {
            return 100;
        }
        if (userCourse.getEnrolled() != null) {
            return 5; // Минимальный прогресс для начатого курса
        }
        return 0;
    }
}
