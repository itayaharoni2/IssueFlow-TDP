package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Role: Handles database access and queries for project.
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** Active (non-deleted) projects */
    List<Project> findAllByDeletedAtIsNull();

    /** Soft-deleted projects */
    List<Project> findAllByDeletedAtIsNotNull();

    /** Active project by ID */
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
}
