package com.freelance.freelancepm.repository;

import com.freelance.freelancepm.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> {

}
