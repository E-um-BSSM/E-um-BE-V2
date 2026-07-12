package com.example.eumbev2.repository.assignment;

import com.example.eumbev2.entity.assignment.Assignment;
import com.example.eumbev2.entity.classroom.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Page<Assignment> findByClassroomOrderByCreatedAtDesc(Classroom classroom, Pageable pageable);

    List<Assignment> findByClassroom(Classroom classroom);
}
