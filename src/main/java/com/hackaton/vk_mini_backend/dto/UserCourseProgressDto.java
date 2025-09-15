package com.hackaton.vk_mini_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
public class UserCourseProgressDto {
    private Long courseId;
    private String courseTitle;
    private String courseCategory;
    private String courseDescription;
    private String avatarUrl;
    private Integer progress; // Прогресс в процентах (0-100)
    private Integer score; // Средний балл по тестам
    private Timestamp enrolled;
    private Timestamp completed;
    private String status; // ALIVE, FAVORITE, etc.
}
