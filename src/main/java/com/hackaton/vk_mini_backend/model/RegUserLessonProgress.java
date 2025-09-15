package com.hackaton.vk_mini_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
@Entity
@Table(name = "reg_user_course", schema = "public")
public class RegUserLessonProgress {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private ClsUser user;

    @ManyToOne
    @JoinColumn(name = "id_lesson", referencedColumnName = "id")
    private ClsLesson lesson;

    @Column(name = "video_completed")
    private Boolean videoCompleted;

    @Column(name = "lecture_completed")
    private Boolean lectureCompleted;

    @Column(name = "quiz_completed")
    private Boolean quizCompleted;
}
