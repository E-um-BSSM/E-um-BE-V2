package com.example.eumbev2.entity.classroom;

import com.example.eumbev2.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Merges ERD `waiting_list` + `classroom_members` into a single table with a status column,
 * matching the API spec's unified `Member` / `WaitingMember` / `MemberStatus` model:
 * a join request creates a WAITING row (with message + application answers already recorded
 * separately), mentor accept flips it to ACCEPTED, and the "kick/reject" endpoint deletes the row.
 */
@Entity
@Table(name = "classroom_members", uniqueConstraints = @UniqueConstraint(columnNames = {"classroom_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Lob
    private String message;

    @Column(nullable = false)
    private Instant appliedAt;

    private Instant joinedAt;
}
