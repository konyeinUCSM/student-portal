package com.manulife.studentportal.teacher;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUserId(Long userId);

    // Include deleted records — staffId is permanently reserved once used
    @Query(value = "SELECT COUNT(*) FROM teachers WHERE staff_id = ?1", nativeQuery = true)
    long countByStaffIdAllRecords(String staffId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Teacher t JOIN t.classIds ci WHERE t.id = :teacherId AND ci = :classId")
    boolean existsByIdAndClasses_Id(@Param("teacherId") Long teacherId, @Param("classId") Long classId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Teacher t JOIN t.subjectIds si WHERE t.id = :teacherId AND si = :subjectId")
    boolean existsByIdAndSubjects_Id(@Param("teacherId") Long teacherId, @Param("subjectId") Long subjectId);

    @Query("SELECT ci FROM Teacher t JOIN t.classIds ci WHERE t.id = :teacherId")
    List<Long> findClassIdsByTeacherId(@Param("teacherId") Long teacherId);
}