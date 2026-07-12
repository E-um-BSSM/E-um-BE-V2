package com.example.eumbev2.entity.assignment;

import com.example.eumbev2.entity.classroom.Classroom;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private Instant dueDate;

    private Integer difficulty;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;
}
