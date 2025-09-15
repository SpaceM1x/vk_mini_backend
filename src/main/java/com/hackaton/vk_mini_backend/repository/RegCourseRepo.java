package com.hackaton.vk_mini_backend.repository;

import com.hackaton.vk_mini_backend.model.RegCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegCourseRepo extends JpaRepository<RegCourse, Long> {
    @Query("SELECT rc FROM RegCourse rc JOIN RegUserCourse ruc ON rc.id = ruc.course.id "
            + "JOIN ClsCourseStatus cs ON ruc.status.id = cs.id "
            + "WHERE ruc.user.id = :userId AND cs.name = 'FAVORITE'")
    List<RegCourse> findFavoritesByUserId(@Param("userId") Long userId);
}
