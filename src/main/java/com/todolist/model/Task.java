package com.todolist.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("tasks")
public record Task(@Id Long id, String title, TaskStatus status) {
}