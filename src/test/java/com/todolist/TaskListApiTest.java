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
class TaskListApiTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void clearStore() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();
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
        long first = createTask("First");
        long second = createTask("Second");

        webTestClient.get().uri("/api/todos")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(first)
                .jsonPath("$[0].title").isEqualTo("First")
                .jsonPath("$[0].status").isEqualTo("PENDING")
                .jsonPath("$[1].id").isEqualTo(second)
                .jsonPath("$[1].title").isEqualTo("Second")
                .jsonPath("$[1].status").isEqualTo("PENDING");
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