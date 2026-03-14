package com.manulife.studentportal.repository;

import com.manulife.studentportal.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByName(String name);

    Optional<Subject> findByName(String name);
}
