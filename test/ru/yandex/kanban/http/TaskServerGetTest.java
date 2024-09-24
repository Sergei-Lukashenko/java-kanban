package ru.yandex.kanban.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import ru.yandex.kanban.manager.Managers;
import ru.yandex.kanban.manager.TaskManager;
import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskServerGetTest {

    private static final TaskManager manager = Managers.getDefault();
    private static HttpTaskServer taskServer;
    private final Gson gson = Managers.getGson();

    private Task task;
    private Subtask subtask;
    private Epic epic;

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

        task = new Task("Task title", "Task description");
        task.setDuration(Duration.ofMinutes(30));
        manager.addNewTask(task);

        epic = new Epic("Epic title", "Epic description");
        int epicId = manager.addNewEpic(epic);

        subtask = new Subtask("Subtask title", "Subtask description", epicId);
        subtask.setStartTime(subtask.getStartTime().plusMinutes(60));
        subtask.setDuration(Duration.ofMinutes(30));
        manager.addNewSubtask(subtask);
    }

    @AfterAll
    public static void afterAll() {
        taskServer.stop();
    }

    @Test
    void getTasks() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/tasks");
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());

            Type tasksType = new TypeToken<ArrayList<Task>>() {}.getType();
            List<Task> actualTasks = gson.fromJson(response.body(), tasksType);
            assertNotNull(actualTasks, "Task list is null");
            assertEquals(1, actualTasks.size(), "Task list must contain 1 item");
            assertEquals(task, actualTasks.getFirst(), "The task from server is not equal to the initial one");
        }
    }

    @Test
    void getSubtasks() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/subtasks");
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());

            Type subtasksType = new TypeToken<ArrayList<Subtask>>() {
            }.getType();
            List<Task> actualSubtasks = gson.fromJson(response.body(), subtasksType);
            assertNotNull(actualSubtasks, "Subtask list is null");
            assertEquals(1, actualSubtasks.size(), "Subtask list must contain 1 item");
            assertEquals(subtask, actualSubtasks.getFirst(), "The subtask from server is not equal to the initial one");
        }
    }

    @Test
    void getEpics() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/epics");
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());

            Type epicsType = new TypeToken<ArrayList<Epic>>() {}.getType();
            List<Task> actualEpics = gson.fromJson(response.body(), epicsType);
            assertNotNull(actualEpics, "Epic list is null");
            assertEquals(1, actualEpics.size(), "Epic list must contain 1 item");
            assertEquals(epic, actualEpics.getFirst(), "The epic from server is not equal to the initial one");
        }
    }

    @Test
    void getTaskById() throws IOException, InterruptedException {
        final int id = task.getId();
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/tasks/" + id);
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());

            Task actualTask = gson.fromJson(response.body(), Task.class);
            assertNotNull(actualTask, "Task is null");
            assertEquals(task, actualTask, "The task from server is not equal to the initial one");
        }
    }

    @Test
    void getSubtaskById() throws IOException, InterruptedException {
        final int id = subtask.getId();
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/subtasks/" + id);
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());

            Subtask actualTask = gson.fromJson(response.body(), Subtask.class);
            assertNotNull(actualTask, "Subtask is null");
            assertEquals(subtask, actualTask, "The subtask from server is not equal to the initial one");
        }
    }

    @Test
    void getEpicById() throws IOException, InterruptedException {
        final int id = epic.getId();
        try (HttpClient client = HttpClient.newHttpClient()) {
        URI uri = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(200, response.statusCode());

        Epic actualEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(actualEpic, "Epic is null");
        assertEquals(epic, actualEpic, "The epic from server is not equal to the initial one");
        }
    }

    @Test
    void getEpicSubtasks() throws IOException, InterruptedException {
        final int id = epic.getId();
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/epics/" + id + "/subtasks");
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());

            Type subtasksType = new TypeToken<ArrayList<Subtask>>() {
            }.getType();
            List<Task> actualSubtasks = gson.fromJson(response.body(), subtasksType);
            assertNotNull(actualSubtasks, "List of epic subtasks is null");
            assertEquals(1, actualSubtasks.size(), "List of epic subtasks must contain 1 item");
            assertEquals(subtask, actualSubtasks.getFirst(), "The subtask from server is not equal to the initial one");
        }
    }

    @Test
    void getHistory() throws IOException, InterruptedException {
        manager.getTaskById(task.getId());  // to put the task into the history
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/history");
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());

            Type tasksType = new TypeToken<ArrayList<Task>>() {
            }.getType();
            List<Task> actualTasks = gson.fromJson(response.body(), tasksType);
            assertNotNull(actualTasks, "Task history list is null");
            assertEquals(task, actualTasks.getFirst(), "The newest history task is not equal to the triggered one");
        }
    }

    @Test
    void getPrioritized() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            URI uri = URI.create("http://localhost:8080/prioritized");
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertEquals(200, response.statusCode());

            Type tasksType = new TypeToken<ArrayList<Task>>() {
            }.getType();
            List<Task> actualTasks = gson.fromJson(response.body(), tasksType);
            assertNotNull(actualTasks, "Prioritized list is null");
            assertEquals(task, actualTasks.getFirst(), "The 1st prioritized task is not equal to the expected one");
        }
    }
}