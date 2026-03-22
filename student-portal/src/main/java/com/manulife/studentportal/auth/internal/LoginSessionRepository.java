package com.manulife.studentportal.auth.internal;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginSessionRepository extends JpaRepository<LoginSession, Long>, JpaSpecificationExecutor<LoginSession> {

    boolean existsByTokenIdAndActiveTrue(String tokenId);

    Optional<LoginSession> findByTokenId(String tokenId);
    
    long countByActiveTrue();
}