package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Role: Data Access Object for Project entities.
 * It provides operations to query active or soft-deleted projects and perform standard CRUD operations.
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Retrieves all projects that have not been soft-deleted.
     */
    List<Project> findAllByDeletedAtIsNull();

    /**
     * Retrieves all projects that have been soft-deleted.
     */
    List<Project> findAllByDeletedAtIsNotNull();

    /**
     * Retrieves a specific project by its ID, ensuring it has not been soft-deleted.
     */
    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
}
