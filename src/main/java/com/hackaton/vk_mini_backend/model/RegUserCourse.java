package com.hackaton.vk_mini_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
@Entity
@Table(name = "reg_user_course", schema = "public")
public class RegUserCourse {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private ClsUser user;

    @ManyToOne
    @JoinColumn(name = "id_course", referencedColumnName = "id")
    private RegCourse course;

    @ManyToOne
    @JoinColumn(name = "id_course_status", referencedColumnName = "id")
    private ClsCourseStatus status;

    @Column(name = "enrolled")
    private Timestamp enrolled;

    @Column(name = "completed")
    private Timestamp completed;
}
