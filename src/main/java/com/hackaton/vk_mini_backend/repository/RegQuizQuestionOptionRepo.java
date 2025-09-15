package com.hackaton.vk_mini_backend.repository;

import com.hackaton.vk_mini_backend.model.RegQuizQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegQuizQuestionOptionRepo extends JpaRepository<RegQuizQuestionOption, Long> {
    List<RegQuizQuestionOption> findAllByQuestionId(Long questionId);
}
