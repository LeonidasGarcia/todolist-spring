package com.todolist.repository;

import com.todolist.model.Task;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TaskRepository {

    private final Map<Long, Task> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public long nextId() {
        return idGenerator.getAndIncrement();
    }

    public Mono<Task> save(Task task) {
        store.put(task.id(), task);
        return Mono.just(task);
    }

    public Mono<Task> findById(long id) {
        return Mono.justOrEmpty(store.get(id));
    }

    public Flux<Task> findAll() {
        return Flux.fromIterable(store.values().stream()
                .sorted(Comparator.comparingLong(Task::id))
                .toList());
    }

    public Mono<Boolean> deleteById(long id) {
        return Mono.just(store.remove(id) != null);
    }

    public Mono<Void> clear() {
        store.clear();
        idGenerator.set(1L);
        return Mono.empty();
    }
}