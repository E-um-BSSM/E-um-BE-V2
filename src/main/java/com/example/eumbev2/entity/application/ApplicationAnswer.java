package com.example.eumbev2.entity.application;

import com.example.eumbev2.entity.classroom.Classroom;
import com.example.eumbev2.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

/** Maps ERD `application_answers`: one row per (classroom, applicant, question) at join-request time. */
@Entity
@Table(name = "application_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private ApplicationQuestion question;

    @Lob
    private String value;
}
