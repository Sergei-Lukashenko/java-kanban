package ru.yandex.kanban.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.manager.Managers;
import ru.yandex.kanban.manager.TaskManager;
import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class TaskServerDeleteTest {

    private static final TaskManager manager = Managers.getDefault();
    private static HttpTaskServer taskServer;

    private Task taskToDelete;
    private Subtask subtaskToDelete;
    private Epic epicToDelete;

    @BeforeAll
    public static void beforeAll() throws IOException {
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @BeforeEach
    void init() {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();

        taskToDelete = new Task("Task to delete title", "Task to delete description");
        manager.addNewTask(taskToDelete);

        Epic epicToLink = new Epic("Epic to delete title", "Epic to delete description");
        int epicId = manager.addNewEpic(epicToLink);

        subtaskToDelete = new Subtask("Subtask to delete title", "Subtask to delete description", epicId);
        manager.addNewSubtask(subtaskToDelete);

        epicToDelete = new Epic("Epic to delete", "Epic to delete description");
        manager.addNewEpic(epicToDelete);
    }

    @AfterAll
    public static void afterAll() {
        taskServer.stop();
    }

    @Test
    void deleteTaskById() throws IOException, InterruptedException {
        final int id = taskToDelete.getId();
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/tasks/" + id);
            HttpRequest request = HttpRequest.newBuilder(uri).DELETE().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());
        }
        assertNull(manager.getTaskById(id), "Task returned by manager must be null");
    }

    @Test
    void deleteSubtaskById() throws IOException, InterruptedException {
        final int id = subtaskToDelete.getId();
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/subtasks/" + id);
            HttpRequest request = HttpRequest.newBuilder(uri).DELETE().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());
        }
        assertNull(manager.getSubtaskById(id), "Subtask returned by manager must be null");
    }

    @Test
    void deleteEpicById() throws IOException, InterruptedException {
        final int id = epicToDelete.getId();
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/epics/" + id);
            HttpRequest request = HttpRequest.newBuilder(uri).DELETE().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());
        }
        assertNull(manager.getEpicById(id), "Epic returned by manager must be null");
    }
}