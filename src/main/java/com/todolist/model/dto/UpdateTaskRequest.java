package com.todolist.model.dto;

import com.todolist.model.TaskStatus;

public record UpdateTaskRequest(
        String title,
        TaskStatus status
) {
}