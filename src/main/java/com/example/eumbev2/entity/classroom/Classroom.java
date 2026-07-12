package com.example.eumbev2.entity.classroom;

import com.example.eumbev2.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps ERD `classrooms`, with `classroom_tag` simplified from its own table to an
 * `@ElementCollection` (a plain list of strings is all the API ever exposes/accepts for tags).
 */
@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Lob
    private String mentorIntroduction;

    @Lob
    private String guide;

    private String classroomCode;

    private Instant classroomCodeExpiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer difficulty = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessScope accessScope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ClassStatus status = ClassStatus.RECRUITING;

    private String bannerImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User mentor;

    @ElementCollection
    @CollectionTable(name = "classroom_tag", joinColumns = @JoinColumn(name = "classroom_id"))
    @Column(name = "content")
    @OrderColumn(name = "order_no")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public boolean isInviteCodeValid() {
        return classroomCode != null
                && (classroomCodeExpiresAt == null || Instant.now().isBefore(classroomCodeExpiresAt));
    }
}
