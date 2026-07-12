package com.example.eumbev2.entity.submission;

import com.example.eumbev2.entity.assignment.Assignment;
import com.example.eumbev2.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "submissions", uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    private String content;

    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    private Integer score;

    @Lob
    private String feedback;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant submittedAt;

    private Instant gradedAt;
}
