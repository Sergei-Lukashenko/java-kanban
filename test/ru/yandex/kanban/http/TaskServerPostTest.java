package ru.yandex.kanban.http;

import com.google.gson.Gson;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskServerPostTest {

    private static final TaskManager manager = Managers.getDefault();
    private static HttpTaskServer taskServer;
    private final Gson gson = Managers.getGson();

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
    }

    @AfterAll
    public static void afterAll() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        final Task task = new Task("Task to create title", "Task to create description");
        String taskJson = gson.toJson(task);

        try (HttpClient client = HttpClient.newHttpClient()) {

            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest request = HttpRequest.newBuilder().uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
        }

        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "No tasks returned from manager");
        assertEquals(1, tasksFromManager.size(), "Incorrect count of tasks");
        assertEquals("Task to create title", tasksFromManager.getFirst().getTitle(),
                "Incorrect task title");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        final Task task = new Task("Task to update title", "Task to update description");
        manager.addNewTask(task);
        task.setTitle("Task updated!");
        String taskJson = gson.toJson(task);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest request = HttpRequest.newBuilder().uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
        }

        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "No tasks returned from manager");
        assertEquals(1, tasksFromManager.size(), "Incorrect count of tasks");
        assertEquals("Task updated!", tasksFromManager.getFirst().getTitle(), "Incorrect update title");
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic dummyEpic = new Epic("Epic title", "Epic description");
        int epicId = manager.addNewEpic(dummyEpic);

        final Subtask subtask;
        subtask = new Subtask("Subtask to create title", "Subtask to create description", epicId);
        String subtaskJson = gson.toJson(subtask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest request = HttpRequest.newBuilder().uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
        }

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "No subtasks returned from manager");
        assertEquals(1, subtasksFromManager.size(), "Incorrect count of subtasks");
        assertEquals("Subtask to create title", subtasksFromManager.getFirst().getTitle(),
                "Incorrect subtask title");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic dummyEpic = new Epic("Epic title", "Epic description");
        int epicId = manager.addNewEpic(dummyEpic);

        final Subtask subtask;
        subtask = new Subtask("Subtask to update title", "Subtask to update description", epicId);

        manager.addNewSubtask(subtask);
        subtask.setTitle("Subtask updated!");
        String subtaskJson = gson.toJson(subtask);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest request = HttpRequest.newBuilder().uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
        }

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "No subtasks returned from manager");
        assertEquals(1, subtasksFromManager.size(), "Incorrect count of subtasks");
        assertEquals("Subtask updated!", subtasksFromManager.getFirst().getTitle(),
                "Incorrect subtask title");
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic to create title", "Epic to create description");
        String epicJson = gson.toJson(epic);

        try (HttpClient client = HttpClient.newHttpClient()) {
            URI url = URI.create("http://localhost:8080/epics");
            HttpRequest request = HttpRequest.newBuilder().uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());
        }

        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "No epics returned from manager");
        assertEquals(1, epicsFromManager.size(), "Incorrect count of epics");
        assertEquals("Epic to create title", epicsFromManager.getFirst().getTitle(),
                "Incorrect epic title");
    }
}