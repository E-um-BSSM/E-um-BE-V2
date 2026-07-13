package com.example.eumbev2.repository.classroom;

import com.example.eumbev2.entity.classroom.Classroom;
import com.example.eumbev2.entity.classroom.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    Optional<Classroom> findByClassroomCode(String classroomCode);

    @Query("""
            select c from Classroom c
            where (:keyword is null or lower(c.title) like lower(concat('%', :keyword, '%'))
                or lower(c.description) like lower(concat('%', :keyword, '%')))
            and (:difficulty is null or c.difficulty = :difficulty)
            and (:status is null or c.status = :status)
            and c.accessScope = com.example.eumbev2.entity.classroom.AccessScope.PUBLIC
            """)
    Page<Classroom> search(
            @Param("keyword") String keyword,
            @Param("difficulty") Integer difficulty,
            @Param("status") ClassStatus status,
            Pageable pageable
    );

    @Query(value = """
            select c from Classroom c
            left join ClassroomMember m
                on m.classroom = c
                and m.status = com.example.eumbev2.entity.classroom.MemberStatus.ACCEPTED
                and m.role = com.example.eumbev2.entity.classroom.Role.MENTEE
            where (:keyword is null or lower(c.title) like lower(concat('%', :keyword, '%'))
                or lower(c.description) like lower(concat('%', :keyword, '%')))
            and (:difficulty is null or c.difficulty = :difficulty)
            and (:status is null or c.status = :status)
            and c.accessScope = com.example.eumbev2.entity.classroom.AccessScope.PUBLIC
            group by c
            order by count(m) desc
            """,
            countQuery = """
            select count(c) from Classroom c
            where (:keyword is null or lower(c.title) like lower(concat('%', :keyword, '%'))
                or lower(c.description) like lower(concat('%', :keyword, '%')))
            and (:difficulty is null or c.difficulty = :difficulty)
            and (:status is null or c.status = :status)
            and c.accessScope = com.example.eumbev2.entity.classroom.AccessScope.PUBLIC
            """)
    Page<Classroom> searchOrderByPopularity(
            @Param("keyword") String keyword,
            @Param("difficulty") Integer difficulty,
            @Param("status") ClassStatus status,
            Pageable pageable
    );
}
