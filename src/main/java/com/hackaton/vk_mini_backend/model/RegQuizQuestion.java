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
@Table(name = "reg_quiz_question", schema = "public")
public class RegQuizQuestion {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_quiz", referencedColumnName = "id")
    private ClsQuiz quiz;

    @Column(name = "question_text")
    private String text;

    @Column(name = "points")
    private Integer points = 1;

    @Column(name = "question_order", nullable = false)
    private Integer order;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RegQuizQuestionOption> options;
}
