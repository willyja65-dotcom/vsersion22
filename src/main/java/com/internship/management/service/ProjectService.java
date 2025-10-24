package com.internship.management.service;

import com.internship.management.dto.ProjectDTO;
import com.internship.management.entity.Encadreur;
import com.internship.management.entity.Intern;
import com.internship.management.entity.Project;
import com.internship.management.repository.EncadreurRepository;
import com.internship.management.repository.InternRepository;
import com.internship.management.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final InternRepository internRepository;
    private final EncadreurRepository encadreurRepository;
    private final ActivityHistoryService activityHistoryService;

    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findByDeletedFalse().stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByEncadreur(Long encadreurId) {
        Encadreur encadreur = encadreurRepository.findById(encadreurId)
                .orElseThrow(() -> new RuntimeException("ENCADREUR_NOT_FOUND"));

        return projectRepository.findByEncadreurAndDeletedFalse(encadreur).stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByStagiaire(Long stagiaireId) {
        Intern intern = internRepository.findById(stagiaireId)
                .orElseThrow(() -> new RuntimeException("INTERN_NOT_FOUND"));

        return projectRepository.findByInternsContaining(intern.getId()).stream()
                .map(ProjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findByIdWithInterns(id)
                .orElseThrow(() -> new RuntimeException("PROJECT_NOT_FOUND"));
        return ProjectDTO.fromEntity(project);
    }

    @Transactional
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        Project project = new Project();
        project.setTitle(projectDTO.getTitle());
        project.setDescription(projectDTO.getDescription());
        project.setStartDate(projectDTO.getStartDate());
        project.setEndDate(projectDTO.getEndDate());
        project.setDepartment(projectDTO.getDepartment());
        project.setProgress(projectDTO.getProgress() != null ? projectDTO.getProgress() : 0);
        project.setStatus(projectDTO.getStatus() != null ?
                Project.ProjectStatus.valueOf(projectDTO.getStatus()) : Project.ProjectStatus.PLANNING);

        if (projectDTO.getEncadreurId() != null) {
            Encadreur encadreur = encadreurRepository.findById(projectDTO.getEncadreurId())
                    .orElseThrow(() -> new RuntimeException("ENCADREUR_NOT_FOUND"));
            project.setEncadreur(encadreur);
        }

        if (projectDTO.getStagiaireId() != null) {
            project.setStagiaireId(projectDTO.getStagiaireId());
        }

        Project savedProject = projectRepository.save(project);

        activityHistoryService.logActivity(
            savedProject.getEncadreur() != null ? savedProject.getEncadreur().getUser().getId() : null,
            "CREATE",
            "PROJECT",
            savedProject.getId(),
            "Création du projet: " + savedProject.getTitle()
        );

        return ProjectDTO.fromEntity(savedProject);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PROJECT_NOT_FOUND"));

        if (projectDTO.getTitle() != null) project.setTitle(projectDTO.getTitle());
        if (projectDTO.getDescription() != null) project.setDescription(projectDTO.getDescription());
        if (projectDTO.getStartDate() != null) project.setStartDate(projectDTO.getStartDate());
        if (projectDTO.getEndDate() != null) project.setEndDate(projectDTO.getEndDate());
        if (projectDTO.getDepartment() != null) project.setDepartment(projectDTO.getDepartment());
        if (projectDTO.getProgress() != null) project.setProgress(projectDTO.getProgress());
        if (projectDTO.getStatus() != null)
            project.setStatus(Project.ProjectStatus.valueOf(projectDTO.getStatus()));

        if (projectDTO.getEncadreurId() != null) {
            Encadreur encadreur = encadreurRepository.findById(projectDTO.getEncadreurId())
                    .orElseThrow(() -> new RuntimeException("ENCADREUR_NOT_FOUND"));
            project.setEncadreur(encadreur);
        }

        if (projectDTO.getStagiaireId() != null) {
            project.setStagiaireId(projectDTO.getStagiaireId());
        }

        Project updatedProject = projectRepository.save(project);

        activityHistoryService.logActivity(
            updatedProject.getEncadreur() != null ? updatedProject.getEncadreur().getUser().getId() : null,
            "UPDATE",
            "PROJECT",
            updatedProject.getId(),
            "Mise à jour du projet: " + updatedProject.getTitle()
        );

        return ProjectDTO.fromEntity(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PROJECT_NOT_FOUND"));
        project.setDeleted(true);
        projectRepository.save(project);

        activityHistoryService.logActivity(
            project.getEncadreur() != null ? project.getEncadreur().getUser().getId() : null,
            "DELETE",
            "PROJECT",
            project.getId(),
            "Suppression du projet: " + project.getTitle()
        );
    }

    @Transactional
    public ProjectDTO assignInterns(Long projectId, List<Long> internIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("PROJECT_NOT_FOUND"));

        List<Intern> interns = internRepository.findAllById(internIds);
        if (interns.size() != internIds.size()) {
            throw new RuntimeException("INTERN_NOT_FOUND");
        }

        for (Intern intern : interns) {
            intern.setProject(project);
        }
        internRepository.saveAll(interns);
        internRepository.flush();

        project = projectRepository.findByIdWithInterns(projectId)
                .orElseThrow(() -> new RuntimeException("PROJECT_NOT_FOUND"));

        return ProjectDTO.fromEntity(project);
    }
}
