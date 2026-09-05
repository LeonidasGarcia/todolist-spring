package com.todolist.repository;

import com.todolist.model.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TaskRepository extends ReactiveCrudRepository<Task, Long> {

    Flux<Task> findAllByOrderByIdAsc();
}