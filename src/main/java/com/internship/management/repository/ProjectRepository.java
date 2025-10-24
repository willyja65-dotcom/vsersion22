package com.internship.management.repository;

import com.internship.management.entity.Encadreur;
import com.internship.management.entity.Intern;
import com.internship.management.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByEncadreurIdAndDeletedFalse(Long encadreurId);
    List<Project> findByDepartmentAndDeletedFalse(String department);
    List<Project> findByEncadreurAndDeletedFalse(Encadreur encadreur);
    List<Project> findByDeletedFalse();

    @Query("SELECT p FROM Project p JOIN p.interns i WHERE i.id = :internId AND p.deleted = false")
    List<Project> findByInternsContaining(@Param("internId") Long internId);

    @Query("SELECT p FROM Project p JOIN p.interns i WHERE i.id = :internId AND p.deleted = false")
    List<Project> findByInternId(@Param("internId") Long internId);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.interns WHERE p.id = :id")
    Optional<Project> findByIdWithInterns(@Param("id") Long id);

}
