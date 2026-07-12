package com.example.eumbev2.repository.application;

import com.example.eumbev2.entity.application.ApplicationAnswer;
import com.example.eumbev2.entity.classroom.Classroom;
import com.example.eumbev2.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationAnswerRepository extends JpaRepository<ApplicationAnswer, Long> {

    List<ApplicationAnswer> findByClassroomAndUser(Classroom classroom, User user);

    void deleteByClassroomAndUser(Classroom classroom, User user);

    void deleteByClassroom(Classroom classroom);
}
