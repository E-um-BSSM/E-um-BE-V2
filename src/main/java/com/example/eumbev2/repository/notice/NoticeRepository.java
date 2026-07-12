package com.example.eumbev2.repository.notice;

import com.example.eumbev2.entity.classroom.Classroom;
import com.example.eumbev2.entity.notice.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByClassroomOrderByCreatedAtDesc(Classroom classroom, Pageable pageable);

    void deleteByClassroom(Classroom classroom);
}
