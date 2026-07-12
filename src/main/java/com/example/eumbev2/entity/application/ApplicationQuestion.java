package com.example.eumbev2.entity.application;

import com.example.eumbev2.entity.classroom.Classroom;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps ERD `application_questions`. `application_question_options` is simplified from its own
 * table to an `@ElementCollection` (an ordered list of option strings), since the API never
 * exposes option IDs — only SINGLE_CHOICE questions populate it.
 */
@Entity
@Table(name = "application_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false)
    private Integer orderNo;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean required = false;

    private Integer maxLength;

    @ElementCollection
    @CollectionTable(name = "application_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "content")
    @OrderColumn(name = "order_no")
    @Builder.Default
    private List<String> options = new ArrayList<>();
}
