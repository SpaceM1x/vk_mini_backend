package com.hackaton.vk_mini_backend.service;

import com.hackaton.vk_mini_backend.model.ClsLesson;
import com.hackaton.vk_mini_backend.repository.ClsLessonRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClsLessonService {

    private final ClsLessonRepo clsLessonRepo;

    public List<ClsLesson> findByCourseId(Long courseId) {
        return clsLessonRepo.findByCourseId(courseId);
    }
}
