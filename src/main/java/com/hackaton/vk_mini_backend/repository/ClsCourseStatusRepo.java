package com.hackaton.vk_mini_backend.repository;

import com.hackaton.vk_mini_backend.model.ClsCourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClsCourseStatusRepo extends JpaRepository<ClsCourseStatus, Long> {
    Optional<ClsCourseStatus> findByName(String name);
}
