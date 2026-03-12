package com.manulife.studentportal.repository;

import com.manulife.studentportal.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserId(Long userId);

    boolean existsByIdAndClasses_Id(Long teacherId, Long classId);

    boolean existsByIdAndSubjects_Id(Long teacherId, Long subjectId);
}