package com.todolist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.todolist.repository.TaskRepository;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class TaskUpdateApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository repository;

    private long taskId;

    @BeforeEach
    void clearAndCreateTask() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();
        AtomicLong id = new AtomicLong();
        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Buy groceries\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(value -> id.set(Long.parseLong(value.toString())));
        taskId = id.get();
    }

    @Test
    void patchTitleOnlyChangesTitleKeepsStatus() {
        webTestClient.patch().uri("/api/todos/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Buy groceries and milk\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").value(value -> org.assertj.core.api.Assertions.assertThat(Long.parseLong(value.toString())).isEqualTo(taskId))
                .jsonPath("$.title").isEqualTo("Buy groceries and milk")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void patchStatusOnlyChangesStatusKeepsTitle() {
        webTestClient.patch().uri("/api/todos/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"status\":\"COMPLETED\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").value(value -> org.assertj.core.api.Assertions.assertThat(Long.parseLong(value.toString())).isEqualTo(taskId))
                .jsonPath("$.title").isEqualTo("Buy groceries")
                .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    void patchBothFieldsChangesBoth() {
        webTestClient.patch().uri("/api/todos/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Updated\",\"status\":\"COMPLETED\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").value(value -> org.assertj.core.api.Assertions.assertThat(Long.parseLong(value.toString())).isEqualTo(taskId))
                .jsonPath("$.title").isEqualTo("Updated")
                .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    void patchEmptyBodyReturns400() {
        webTestClient.patch().uri("/api/todos/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void patchInvalidStatusReturns400() {
        webTestClient.patch().uri("/api/todos/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"status\":\"DONE\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void patchBlankTitleReturns400() {
        webTestClient.patch().uri("/api/todos/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"   \"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void patchUnknownIdReturns404() {
        webTestClient.patch().uri("/api/todos/{id}", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"status\":\"COMPLETED\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Task with id 999 not found");
    }
}