package com.example.eumbev2.repository.submission;

import com.example.eumbev2.entity.assignment.Assignment;
import com.example.eumbev2.entity.submission.Submission;
import com.example.eumbev2.entity.submission.SubmissionStatus;
import com.example.eumbev2.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByAssignmentAndUser(Assignment assignment, User user);

    boolean existsByAssignmentAndUser(Assignment assignment, User user);

    Page<Submission> findByAssignmentAndStatus(Assignment assignment, SubmissionStatus status, Pageable pageable);

    Page<Submission> findByAssignment(Assignment assignment, Pageable pageable);

    long countByAssignment(Assignment assignment);

    void deleteByAssignment(Assignment assignment);
}
