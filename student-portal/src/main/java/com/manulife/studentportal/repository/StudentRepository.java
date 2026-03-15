package com.manulife.studentportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.manulife.studentportal.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    List<Student> findBySchoolClassId(Long classId, Pageable pageable);

    Page<Student> findBySchoolClassIdIn(List<Long> classIds, Pageable pageable);

    // Include deleted records — rollNumber is permanently reserved once used
    @Query(value = "SELECT COUNT(*) FROM students WHERE roll_number = ?1", nativeQuery = true)
    long countByRollNumberAllRecords(String rollNumber);

    boolean existsByUserId(Long userId);

    boolean existsBySchoolClassId(Long classId);
}