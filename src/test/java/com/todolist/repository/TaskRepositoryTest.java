package com.todolist.repository;

import com.todolist.model.Task;
import com.todolist.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

class TaskRepositoryTest {

    private TaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TaskRepository();
    }

    @Test
    void saveStoresTaskAndFindByIdRetrievesIt() {
        Task task = new Task(repository.nextId(), "Buy groceries", TaskStatus.PENDING);

        StepVerifier.create(repository.save(task))
                .expectNext(task)
                .verifyComplete();

        StepVerifier.create(repository.findById(task.id()))
                .expectNext(task)
                .verifyComplete();
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        StepVerifier.create(repository.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAllReturnsTasksSortedByIdAscending() {
        Task first = new Task(repository.nextId(), "first", TaskStatus.PENDING);
        Task second = new Task(repository.nextId(), "second", TaskStatus.COMPLETED);
        Task third = new Task(repository.nextId(), "third", TaskStatus.PENDING);

        List<Task> tasksInOrder = List.of(first, second, third);

        repository.save(second).block();
        repository.save(third).block();
        repository.save(first).block();

        StepVerifier.create(repository.findAll())
                .expectNext(first, second, third)
                .verifyComplete();
    }

    @Test
    void findAllReturnsEmptyWhenStoreIsEmpty() {
        StepVerifier.create(repository.findAll())
                .verifyComplete();
    }

    @Test
    void deleteByIdRemovesTaskAndReports() {
        Task task = new Task(repository.nextId(), "to delete", TaskStatus.PENDING);
        repository.save(task).block();

        StepVerifier.create(repository.deleteById(task.id()))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(repository.findById(task.id()))
                .verifyComplete();

        StepVerifier.create(repository.deleteById(task.id()))
                .expectNext(false)
                .verifyComplete();
    }
}