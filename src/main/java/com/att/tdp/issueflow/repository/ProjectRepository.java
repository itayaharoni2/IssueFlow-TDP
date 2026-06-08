package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
/**
 * Role: Data Access Object for Project entities.
 * It provides operations to query active or soft-deleted projects and perform
 * standard CRUD operations.
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Retrieves all active (non-deleted) projects.
     */
    Page<Project> findAllByDeletedAtIsNull(Pageable pageable);

    /**
     * Retrieves all soft-deleted projects.
     */
    Page<Project> findAllByDeletedAtIsNotNull(Pageable pageable);

    // Retrieves a specific project by its ID, ensuring it has not been
    // soft-deleted.
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
}
