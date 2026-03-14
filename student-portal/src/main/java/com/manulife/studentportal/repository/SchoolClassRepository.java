package com.manulife.studentportal.repository;

import com.manulife.studentportal.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    boolean existsByName(String name);

    Optional<SchoolClass> findByName(String name);
}
