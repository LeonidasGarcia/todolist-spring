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
class TaskDeleteApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void clearStore() {
        StepVerifier.create(repository.clear()).verifyComplete();
        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Buy groceries\"}")
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void deleteExistingReturns204AndRemovesFromList() {
        webTestClient.delete().uri("/api/todos/{id}", 1)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        webTestClient.get().uri("/api/todos")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("[]");
    }

    @Test
    void deleteSameTaskAgainReturns404() {
        webTestClient.delete().uri("/api/todos/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.delete().uri("/api/todos/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Task with id 1 not found");
    }

    @Test
    void deleteUnknownIdReturns404() {
        webTestClient.delete().uri("/api/todos/{id}", 999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }
}