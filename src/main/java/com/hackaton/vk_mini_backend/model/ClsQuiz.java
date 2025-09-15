package com.hackaton.vk_mini_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
@Entity
@Table(name = "cls_quiz", schema = "public")
public class ClsQuiz {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_lesson", referencedColumnName = "id")
    private ClsLesson lesson;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "passing_score")
    private Integer passingScore = 70;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RegQuizQuestion> questions;
}
