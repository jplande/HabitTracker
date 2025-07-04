package com.habittracker.repository;

import com.habittracker.entity.User;
import com.habittracker.entity.User.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    Page<User> findByRole(User.Role role, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    long countByIsActive(Boolean isActive);

    Page<User> findByCreatedAtAfter(LocalDateTime date, Pageable pageable);

    /**
     * Compte les utilisateurs par rôle
     */
    long countByRole(Role role);
}