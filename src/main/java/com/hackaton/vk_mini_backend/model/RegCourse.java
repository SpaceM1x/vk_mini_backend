package com.hackaton.vk_mini_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
@Entity
@Table(name = "reg_course", schema = "public")
public class RegCourse {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @ManyToOne
    @JoinColumn(name = "id_course_category", referencedColumnName = "id")
    private ClsCourseCategory courseCategory;
}
