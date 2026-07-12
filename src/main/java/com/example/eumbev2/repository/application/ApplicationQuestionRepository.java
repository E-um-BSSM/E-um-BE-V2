package com.example.eumbev2.repository.application;

import com.example.eumbev2.entity.application.ApplicationQuestion;
import com.example.eumbev2.entity.classroom.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationQuestionRepository extends JpaRepository<ApplicationQuestion, Long> {

    List<ApplicationQuestion> findByClassroomOrderByOrderNoAsc(Classroom classroom);

    void deleteByClassroom(Classroom classroom);
}
