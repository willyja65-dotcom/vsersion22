package com.internship.management.repository;

import com.internship.management.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectIdAndDeletedFalse(Long projectId);
    List<Task> findByStatusAndDeletedFalse(Task.TaskStatus status);
    List<Task> findByDeletedFalse();
}
