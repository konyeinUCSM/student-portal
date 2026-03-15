package com.manulife.studentportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.manulife.studentportal.entity.Teacher;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserId(Long userId);

    boolean existsByStaffId(String staffId);

    boolean existsByIdAndClasses_Id(Long teacherId, Long classId);

    boolean existsByIdAndSubjects_Id(Long teacherId, Long subjectId);
}