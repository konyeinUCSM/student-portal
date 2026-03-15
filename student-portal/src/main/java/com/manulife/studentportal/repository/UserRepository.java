package com.manulife.studentportal.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.manulife.studentportal.entity.User;
import com.manulife.studentportal.enums.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    // Include deleted records — username and email are permanently reserved once used.
    // Native queries return Long from JDBC; callers compare with > 0.
    @Query(value = "SELECT COUNT(*) FROM users WHERE username = ?1", nativeQuery = true)
    long countByUsernameAllRecords(String username);

    @Query(value = "SELECT COUNT(*) FROM users WHERE email = ?1", nativeQuery = true)
    long countByEmailAllRecords(String email);

    @Query(value = "SELECT COUNT(*) FROM users WHERE email = ?1 AND id != ?2", nativeQuery = true)
    long countByEmailExcludingIdAllRecords(String email, Long id);

    Page<User> findByRole(Role role, Pageable pageable);

    long countByRole(Role role);

}