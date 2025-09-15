package com.hackaton.vk_mini_backend.repository;

import com.hackaton.vk_mini_backend.model.RegUserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegUserCourseRepo extends JpaRepository<RegUserCourse, Long> {

    @Query("SELECT uc FROM RegUserCourse uc WHERE uc.user.id = :userId")
    List<RegUserCourse> findByUserId(@Param("userId") Long userId);

    @Query("SELECT uc FROM RegUserCourse uc WHERE uc.user.id = :userId AND uc.course.id = :courseId")
    Optional<RegUserCourse> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT uc FROM RegUserCourse uc WHERE uc.user.id = :userId AND uc.enrolled IS NOT NULL")
    List<RegUserCourse> findStartedCoursesByUserId(@Param("userId") Long userId);
}
