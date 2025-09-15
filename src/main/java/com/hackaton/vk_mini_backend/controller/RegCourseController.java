package com.hackaton.vk_mini_backend.controller;

import com.hackaton.vk_mini_backend.model.RegCourse;
import com.hackaton.vk_mini_backend.repository.ClsUserRepo;
import com.hackaton.vk_mini_backend.service.RegCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Контроллер курсов", description = "Действия с таблицей курсов")
public class RegCourseController {
    private final RegCourseService regCourseService;

    private final ClsUserRepo clsUserRepo;

    @GetMapping("/{id}")
    @Operation(summary = "Получить курс по ID")
    public ResponseEntity<RegCourse> findById(@PathVariable final Long id) {
        ResponseEntity<RegCourse> responseEntity =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        try {
            RegCourse course = regCourseService.findById(id);
            responseEntity = ResponseEntity.ok(course);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return responseEntity;
    }

    @GetMapping("/list")
    @Operation(summary = "Получить список всех курсов")
    public ResponseEntity<List<RegCourse>> findAll() {
        ResponseEntity<List<RegCourse>> responseEntity =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        try {
            List<RegCourse> course = regCourseService.findAll();
            responseEntity = ResponseEntity.ok(course);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return responseEntity;
    }

    @GetMapping("/favorites")
    @Operation(summary = "Получить список курсов в избранном у пользователя")
    public ResponseEntity<List<RegCourse>> findFavoritesByUserId() {
        ResponseEntity<List<RegCourse>> responseEntity =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        try {
            String username = ((UserDetails) SecurityContextHolder.getContext()
                            .getAuthentication()
                            .getPrincipal())
                    .getUsername();
            Long userId = clsUserRepo
                    .findByLoginIgnoreCaseAndIsDeleted(username, false)
                    .get()
                    .getId();
            List<RegCourse> courses = regCourseService.findFavoritesByUserId(userId);
            responseEntity = ResponseEntity.ok(courses);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        return responseEntity;
    }
}
