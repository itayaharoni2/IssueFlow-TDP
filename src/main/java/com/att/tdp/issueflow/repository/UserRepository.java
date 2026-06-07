package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Role: Data Access Object for User entities.
 * It provides custom methods to look up users by username or email, verify uniqueness, and filter by roles.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user by their exact username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Retrieves a user by their username, ignoring case sensitivity.
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Checks if a user already exists with the given username.
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user already exists with the given email address.
     */
    boolean existsByEmail(String email);

    /**
     * Retrieves all users that have a specific role assigned.
     */
    List<User> findAllByRole(Role role);
}
