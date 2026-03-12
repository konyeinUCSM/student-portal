package com.manulife.studentportal.repository;

import com.manulife.studentportal.entity.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {

    boolean existsByTokenIdAndActiveTrue(String tokenId);

    Optional<LoginSession> findByTokenId(String tokenId);
}