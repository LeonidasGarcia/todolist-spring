package com.todolist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.todolist.repository.TaskRepository;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TaskUpdateApiTest {

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
    void patchTitleOnlyChangesTitleKeepsStatus() {
        webTestClient.patch().uri("/api/todos/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Buy groceries and milk\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Buy groceries and milk")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void patchStatusOnlyChangesStatusKeepsTitle() {
        webTestClient.patch().uri("/api/todos/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"status\":\"COMPLETED\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Buy groceries")
                .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    void patchBothFieldsChangesBoth() {
        webTestClient.patch().uri("/api/todos/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Updated\",\"status\":\"COMPLETED\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Updated")
                .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    void patchEmptyBodyReturns400() {
        webTestClient.patch().uri("/api/todos/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void patchInvalidStatusReturns400() {
        webTestClient.patch().uri("/api/todos/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"status\":\"DONE\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void patchBlankTitleReturns400() {
        webTestClient.patch().uri("/api/todos/{id}", 1)
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