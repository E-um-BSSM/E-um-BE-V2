package com.example.eumbev2.repository.classroom;

import com.example.eumbev2.entity.classroom.Classroom;
import com.example.eumbev2.entity.classroom.ClassroomMember;
import com.example.eumbev2.entity.classroom.MemberStatus;
import com.example.eumbev2.entity.classroom.Role;
import com.example.eumbev2.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClassroomMemberRepository extends JpaRepository<ClassroomMember, Long> {

    Optional<ClassroomMember> findByClassroomAndUser(Classroom classroom, User user);

    boolean existsByClassroomAndUser(Classroom classroom, User user);

    long countByClassroomAndStatusAndRole(Classroom classroom, MemberStatus status, Role role);

    Page<ClassroomMember> findByClassroomAndStatus(Classroom classroom, MemberStatus status, Pageable pageable);

    void deleteByClassroom(Classroom classroom);

    @Query("""
            select m.classroom from ClassroomMember m
            where m.user = :user
            and (:role is null or m.role = :role)
            and (:classStatus is null or m.classroom.status = :classStatus)
            and (:membership is null or m.status = :membership)
            order by m.appliedAt desc
            """)
    Page<Classroom> findMyClasses(
            @Param("user") User user,
            @Param("role") Role role,
            @Param("classStatus") com.example.eumbev2.entity.classroom.ClassStatus classStatus,
            @Param("membership") MemberStatus membership,
            Pageable pageable
    );
}
