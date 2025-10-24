package com.internship.management.service;

import com.internship.management.dto.TaskDTO;
import com.internship.management.entity.Project;
import com.internship.management.entity.Task;
import com.internship.management.entity.User;
import com.internship.management.repository.ProjectRepository;
import com.internship.management.repository.TaskRepository;
import com.internship.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ActivityHistoryService activityHistoryService;

    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findByDeletedFalse().stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectIdAndDeletedFalse(projectId).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByIntern(Long internId) {
        List<Task> allTasks = taskRepository.findAll();
        return allTasks.stream()
                .filter(task -> task.getProject() != null &&
                        task.getProject().getInterns() != null &&
                        task.getProject().getInterns().stream()
                                .anyMatch(intern -> intern.getId().equals(internId)))
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByStatus(String status) { // ✅ String → Enum
        Task.TaskStatus enumStatus = Task.TaskStatus.valueOf(status);
        return taskRepository.findByStatusAndDeletedFalse(enumStatus).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return TaskDTO.fromEntity(task);
    }

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ?
                Task.TaskStatus.valueOf(taskDTO.getStatus()) : Task.TaskStatus.TODO);
        task.setPriority(taskDTO.getPriority() != null ?
                Task.TaskPriority.valueOf(taskDTO.getPriority()) : Task.TaskPriority.MEDIUM);
        task.setDueDate(taskDTO.getDueDate());

        if (taskDTO.getProjectId() != null) {
            Project project = projectRepository.findById(taskDTO.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            task.setProject(project);
        }

        Task savedTask = taskRepository.save(task);

        if (savedTask.getProject() != null && savedTask.getProject().getEncadreur() != null) {
            activityHistoryService.logActivity(
                savedTask.getProject().getEncadreur().getUser().getId(),
                "CREATE",
                "TASK",
                savedTask.getId(),
                "Création de la tâche: " + savedTask.getTitle()
            );
        }

        return TaskDTO.fromEntity(savedTask);
    }

    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (taskDTO.getTitle() != null) task.setTitle(taskDTO.getTitle());
        if (taskDTO.getDescription() != null) task.setDescription(taskDTO.getDescription());
        if (taskDTO.getStatus() != null) task.setStatus(Task.TaskStatus.valueOf(taskDTO.getStatus()));
        if (taskDTO.getPriority() != null) task.setPriority(Task.TaskPriority.valueOf(taskDTO.getPriority()));
        if (taskDTO.getDueDate() != null) task.setDueDate(taskDTO.getDueDate());

        if (taskDTO.getProjectId() != null) {
            Project project = projectRepository.findById(taskDTO.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            task.setProject(project);
        }

        Task updatedTask = taskRepository.save(task);

        if (updatedTask.getProject() != null && updatedTask.getProject().getEncadreur() != null) {
            activityHistoryService.logActivity(
                updatedTask.getProject().getEncadreur().getUser().getId(),
                "UPDATE",
                "TASK",
                updatedTask.getId(),
                "Mise à jour de la tâche: " + updatedTask.getTitle()
            );
        }

        return TaskDTO.fromEntity(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setDeleted(true);
        taskRepository.save(task);

        if (task.getProject() != null && task.getProject().getEncadreur() != null) {
            activityHistoryService.logActivity(
                task.getProject().getEncadreur().getUser().getId(),
                "DELETE",
                "TASK",
                task.getId(),
                "Suppression de la tâche: " + task.getTitle()
            );
        }
    }

    @Transactional
    public TaskDTO updateTaskStatus(Long id, String status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(Task.TaskStatus.valueOf(status));
        Task updatedTask = taskRepository.save(task);

        if (updatedTask.getProject() != null && updatedTask.getProject().getEncadreur() != null) {
            activityHistoryService.logActivity(
                updatedTask.getProject().getEncadreur().getUser().getId(),
                "UPDATE",
                "TASK",
                updatedTask.getId(),
                "Changement de statut de la tâche: " + updatedTask.getTitle() + " -> " + status
            );
        }

        return TaskDTO.fromEntity(updatedTask);
    }
}
