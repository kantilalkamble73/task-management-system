package com.taskflow.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskflow.entity.Task;
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTitleContainingIgnoreCase(String keyword);
    List<Task> findByStatus(String status);

}