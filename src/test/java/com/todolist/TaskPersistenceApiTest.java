package com.todolist;

import com.todolist.model.TaskStatus;
import com.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class TaskPersistenceApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void clearStore() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();
    }

    @Test
    void tasksSurviveAFreshApplicationContextAgainstTheSameDatabase() {
        long id = createTask("Survives restart");

        // Simulate a full process restart: a brand-new Spring context boots against
        // the same PostgreSQL test database and must still see the stored row.
        try (ConfigurableApplicationContext fresh =
                     new SpringApplicationBuilder(TodoListApplication.class)
                             .profiles("test")
                             .web(WebApplicationType.NONE)
                             .run()) {
            TaskRepository freshRepository = fresh.getBean(TaskRepository.class);
            StepVerifier.create(freshRepository.findById(id))
                    .expectNextMatches(task -> task.id() == id
                            && task.title().equals("Survives restart")
                            && task.status() == TaskStatus.PENDING)
                    .verifyComplete();
        }
    }

    @Test
    void consecutiveCreatesNeverReuseAnIdentifier() {
        long first = createTask("First");
        long second = createTask("Second");

        assertThat(second).isNotEqualTo(first);
        assertThat(first).isPositive();
        assertThat(second).isPositive();

        StepVerifier.create(repository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    private long createTask(String title) {
        AtomicLong id = new AtomicLong();
        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"" + title + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(value -> id.set(Long.parseLong(value.toString())));
        return id.get();
    }
}