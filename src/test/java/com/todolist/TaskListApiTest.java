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
class TaskListApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void clearStore() {
        StepVerifier.create(repository.clear()).verifyComplete();
    }

    @Test
    void emptyStoreReturnsEmptyArray() {
        webTestClient.get().uri("/api/todos")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("[]");
    }

    @Test
    void listContainsCreatedTasksInAscendingIdOrder() {
        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"First\"}")
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post().uri("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"title\":\"Second\"}")
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get().uri("/api/todos")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].title").isEqualTo("First")
                .jsonPath("$[0].status").isEqualTo("PENDING")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].title").isEqualTo("Second")
                .jsonPath("$[1].status").isEqualTo("PENDING");
    }
}