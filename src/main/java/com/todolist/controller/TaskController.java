package com.todolist.controller;

import com.todolist.model.Task;
import com.todolist.model.dto.CreateTaskRequest;
import com.todolist.model.dto.UpdateTaskRequest;
import com.todolist.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/todos")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Task> create(@RequestBody @Valid CreateTaskRequest request) {
        return taskService.createTask(request.title());
    }

    @GetMapping
    public Flux<Task> findAll() {
        return taskService.findAllTasks();
    }

    @GetMapping("/{id}")
    public Mono<Task> findById(@PathVariable long id) {
        return taskService.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task with id " + id + " not found")));
    }

    @PatchMapping("/{id}")
    public Mono<Task> update(@PathVariable long id, @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable long id) {
        return taskService.deleteTask(id);
    }
}