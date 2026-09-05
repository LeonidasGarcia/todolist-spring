package com.todolist;

import com.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class TaskDeleteApiTest {

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
    void deleteExistingReturns204AndRemovesFromList() {
        webTestClient.delete().uri("/api/todos/{id}", taskId)
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
        webTestClient.delete().uri("/api/todos/{id}", taskId)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.delete().uri("/api/todos/{id}", taskId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Task with id " + taskId + " not found");
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