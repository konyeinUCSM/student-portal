package com.manulife.studentportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.manulife.studentportal.entity.Teacher;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserId(Long userId);

    // Include deleted records — staffId is permanently reserved once used
    @Query(value = "SELECT COUNT(*) FROM teachers WHERE staff_id = ?1", nativeQuery = true)
    long countByStaffIdAllRecords(String staffId);

    boolean existsByIdAndClasses_Id(Long teacherId, Long classId);

    boolean existsByIdAndSubjects_Id(Long teacherId, Long subjectId);

    @Query("SELECT c.id FROM Teacher t JOIN t.classes c WHERE t.id = :teacherId")
    List<Long> findClassIdsByTeacherId(@Param("teacherId") Long teacherId);
}