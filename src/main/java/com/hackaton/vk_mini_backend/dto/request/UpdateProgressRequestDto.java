package com.hackaton.vk_mini_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
public class UpdateProgressRequestDto {
    private Long courseId;
    private Integer progress; // Прогресс в процентах (0-100)
    private Integer score; // Средний балл по тестам (опционально)
}
