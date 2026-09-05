package com.todolist.model;

public record Task(long id, String title, TaskStatus status) {
}