package com.hackaton.vk_mini_backend.repository;

import com.hackaton.vk_mini_backend.model.ClsLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClsLessonRepo extends JpaRepository<ClsLesson, Long> {

    @Query("SELECT l FROM ClsLesson l WHERE l.course.id = :courseId")
    List<ClsLesson> findByCourseId(@Param("courseId") Long courseId);
}
