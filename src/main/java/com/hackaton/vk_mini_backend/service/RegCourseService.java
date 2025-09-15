package com.hackaton.vk_mini_backend.service;

import com.hackaton.vk_mini_backend.exception.NoSuchElementFoundException;
import com.hackaton.vk_mini_backend.model.RegCourse;
import com.hackaton.vk_mini_backend.repository.RegCourseRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegCourseService {
    private final RegCourseRepo courseRepository;

    private static final String COURSE_NOT_FOUND = "curse.not-found";

    public RegCourse findById(final Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new NoSuchElementFoundException(COURSE_NOT_FOUND));
    }

    public List<RegCourse> findAll() {
        return courseRepository.findAll();
    }

    public List<RegCourse> findFavoritesByUserId(final Long id) {
        return courseRepository.findFavoritesByUserId(id);
    }
}
