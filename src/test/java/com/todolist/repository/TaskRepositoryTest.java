package com.todolist.repository;

import com.todolist.model.Task;
import com.todolist.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void clearStore() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();
    }

    @Test
    void saveStoresTaskAndFindByIdRetrievesItWithGeneratedId() {
        Task task = new Task(null, "Buy groceries", TaskStatus.PENDING);

        Task saved = repository.save(task)
                .doOnNext(s -> org.assertj.core.api.Assertions.assertThat(s.id()).isNotNull().isPositive())
                .block();
        org.assertj.core.api.Assertions.assertThat(saved).isNotNull();

        StepVerifier.create(repository.findById(saved.id()))
                .expectNextMatches(found -> found.id().equals(saved.id())
                        && found.title().equals("Buy groceries")
                        && found.status() == TaskStatus.PENDING)
                .verifyComplete();
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        StepVerifier.create(repository.findById(999999L))
                .verifyComplete();
    }

    @Test
    void findAllReturnsTasksSortedByIdAscending() {
        StepVerifier.create(repository.save(new Task(null, "second", TaskStatus.COMPLETED)))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(repository.save(new Task(null, "third", TaskStatus.PENDING)))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier.create(repository.save(new Task(null, "first", TaskStatus.PENDING)))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(repository.findAllByOrderByIdAsc())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findAllReturnsEmptyWhenStoreIsEmpty() {
        StepVerifier.create(repository.findAllByOrderByIdAsc())
                .verifyComplete();
    }

    @Test
    void deleteByIdRemovesTask() {
        long id = repository.save(new Task(null, "to delete", TaskStatus.PENDING))
                .map(Task::id)
                .block();

        StepVerifier.create(repository.deleteById(id))
                .verifyComplete();

        StepVerifier.create(repository.findById(id))
                .verifyComplete();

        StepVerifier.create(repository.existsById(id))
                .expectNext(false)
                .verifyComplete();
    }
}