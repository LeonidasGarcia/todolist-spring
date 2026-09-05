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
class TaskCreateApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void clearStore() {
        StepVerifier.create(repository.clear()).verifyComplete();
    }

    @Test
    void createValidTaskReturns201WithPendingStatus() {
        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Buy groceries\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Buy groceries")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void createBlankTitleReturns400() {
        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"   \"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createMissingTitleReturns400() {
        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createTitleTooLongReturns400() {
        String tooLong = "x".repeat(256);
        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"" + tooLong + "\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }
}