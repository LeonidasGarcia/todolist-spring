package com.todolist.service;

import com.todolist.model.Task;
import com.todolist.model.TaskStatus;
import com.todolist.model.dto.UpdateTaskRequest;
import com.todolist.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TaskService {

    private static final int MAX_TITLE_LENGTH = 255;

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Mono<Task> createTask(String title) {
        String normalized = normalizeTitle(title);
        Task task = new Task(null, normalized, TaskStatus.PENDING);
        return repository.save(task);
    }

    public Flux<Task> findAllTasks() {
        return repository.findAllByOrderByIdAsc();
    }

    public Mono<Task> findById(long id) {
        if (id <= 0) {
            return Mono.error(new IllegalArgumentException("id must be a positive integer"));
        }
        return repository.findById(id);
    }

    public Mono<Task> updateTask(long id, UpdateTaskRequest request) {
        if (id <= 0) {
            return Mono.error(new IllegalArgumentException("id must be a positive integer"));
        }
        if (request.title() == null && request.status() == null) {
            return Mono.error(new IllegalArgumentException("At least one of 'title' or 'status' must be provided"));
        }
        String title = request.title() == null ? null : normalizeTitle(request.title());

        return repository.findById(id)
                .flatMap(task -> {
                    String updatedTitle = title == null ? task.title() : title;
                    TaskStatus updatedStatus = request.status() == null ? task.status() : request.status();
                    return repository.save(new Task(task.id(), updatedTitle, updatedStatus));
                })
                .switchIfEmpty(Mono.error(notFound(id)));
    }

    public Mono<Void> deleteTask(long id) {
        if (id <= 0) {
            return Mono.error(new IllegalArgumentException("id must be a positive integer"));
        }
        return repository.existsById(id)
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        return Mono.error(notFound(id));
                    }
                    return repository.deleteById(id);
                });
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }
        String normalized = title.trim();
        if (normalized.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title must be at most " + MAX_TITLE_LENGTH + " characters");
        }
        return normalized;
    }

    private ResponseStatusException notFound(long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with id " + id + " not found");
    }
}