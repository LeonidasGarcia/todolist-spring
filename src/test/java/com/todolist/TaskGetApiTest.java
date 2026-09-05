package com.todolist;

import com.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TaskGetApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void clearStore() {
        StepVerifier.create(repository.clear()).verifyComplete();
    }

    @Test
    void existingIdReturnsTheTask() {
        long id = createTask("Buy groceries");

        webTestClient.get().uri("/api/todos/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").value(value -> org.assertj.core.api.Assertions.assertThat(Long.parseLong(value.toString())).isEqualTo(id))
                .jsonPath("$.title").isEqualTo("Buy groceries")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void missingIdReturns404WithErrorResponse() {
        webTestClient.get().uri("/api/todos/{id}", 999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Task with id 999 not found");
    }

    @Test
    void nonPositiveIdsReturn400() {
        webTestClient.get().uri("/api/todos/0")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);

        webTestClient.get().uri("/api/todos/-1")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    private long createTask(String title) {
        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"" + title + "\"}")
                .exchange()
                .expectStatus().isCreated();
        return 1L;
    }
}