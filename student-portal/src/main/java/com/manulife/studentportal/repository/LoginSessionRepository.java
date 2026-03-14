package com.manulife.studentportal.repository;

import com.manulife.studentportal.entity.LoginSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {

    boolean existsByTokenIdAndActiveTrue(String tokenId);

    Optional<LoginSession> findByTokenId(String tokenId);

    Page<LoginSession> findAllByOrderByLoginTimeDesc(Pageable pageable);

    Page<LoginSession> findByActiveTrue(Pageable pageable);

    Page<LoginSession> findByActive(boolean active, Pageable pageable);

    Page<LoginSession> findByUserIdAndActive(Long userId, boolean active, Pageable pageable);

    Page<LoginSession> findByUserId(Long userId, Pageable pageable);

    long countByActiveTrue();
}