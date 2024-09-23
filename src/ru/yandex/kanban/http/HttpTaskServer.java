package ru.yandex.kanban.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.kanban.manager.Managers;
import ru.yandex.kanban.manager.TaskManager;
import ru.yandex.kanban.manager.TaskOverlapException;
import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.Task;
import ru.yandex.kanban.tasks.TaskType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

import static ru.yandex.kanban.tasks.TaskType.*;

public class HttpTaskServer extends BaseHttpHandler {
    private static final int PORT = 8080;

    private final HttpServer server;
    private final Gson gson;

    private final TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        gson = Managers.getGson();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", this::handleTasks);
        server.createContext("/subtasks", this::handleSubtasks);
        server.createContext("/epics", this::handleEpics);
        server.createContext("/history", this::handleHistory);
        server.createContext("/prioritized", this::handlePrioritized);
    }

    private void handleTasks(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        switch (requestMethod) {
            case "GET":
                if (Pattern.matches("^/tasks$", requestPath)) {
                    handleGetTasks(exchange, TASK);
                }  else if (Pattern.matches("^/tasks/\\d+$", requestPath)) {
                    int pathId = parseIdFromPath(requestPath, "/tasks/");
                    handleGetTaskById(exchange, TASK, pathId);
                } else {
                    super.handleDefaultGet(exchange);
                }
                break;
            case "DELETE":
                if (Pattern.matches("^/tasks/\\d+$", requestPath)) {
                    int pathId =  parseIdFromPath(requestPath, "/tasks/");
                    handleDeleteTask(exchange, TASK, pathId);
                } else {
                    super.handleDefaultDelete(exchange);
                }
                break;
            case "POST":
                if (Pattern.matches("^/tasks$", requestPath)) {
                    String json = readText(exchange);
                    if (json.isEmpty()) {
                        System.out.println("Body is empty for task in POST request");
                        sendHttpStatus(exchange, 400);  // Bad Request
                        return;
                    }
                    final Task task = gson.fromJson(json, Task.class);
                    final int id = task.getId();
                    try {
                        if (id > 0) {
                            taskManager.updateTask(task);
                            System.out.println("Task updated. ID = " + id);
                        } else {
                            System.out.println("Task created. ID = " + taskManager.addNewTask(task));
                        }
                        sendHttpStatus(exchange, 201);  // Created
                    } catch (TaskOverlapException exception) {
                        System.out.println(exception);
                        sendHttpStatus(exchange, 406);  // Not Acceptable
                    }
                } else {
                    super.handleDefaultPost(exchange);
                }
                break;
            default:
                System.out.println("/tasks[id] path expected for GET/DELETE/POST method but " + requestPath
                        + " got for " + requestMethod + " method");
                super.handleDefaultGet(exchange);
        }
    }

    private void handleSubtasks(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        switch (requestMethod) {
            case "GET":
                if (Pattern.matches("^/subtasks$", requestPath)) {
                    handleGetTasks(exchange, SUBTASK);
                }  else if (Pattern.matches("^/subtasks/\\d+$", requestPath)) {
                    int pathId = parseIdFromPath(requestPath, "/subtasks/");
                    handleGetTaskById(exchange, SUBTASK, pathId);
                } else {
                    super.handleDefaultGet(exchange);
                }
                break;
            case "DELETE":
                if (Pattern.matches("^/subtasks/\\d+$", requestPath)) {
                    int pathId =  parseIdFromPath(requestPath, "/subtasks/");
                    handleDeleteTask(exchange, SUBTASK, pathId);
                } else {
                    super.handleDefaultDelete(exchange);
                }
                break;
            case "POST":
                if (Pattern.matches("^/subtasks$", requestPath)) {
                    String json = readText(exchange);
                    if (json.isEmpty()) {
                        System.out.println("Body is empty for subtask in POST request");
                        sendHttpStatus(exchange, 400);  // Bad Request
                        return;
                    }
                    final Subtask subtask = gson.fromJson(json, Subtask.class);
                    final int id = subtask.getId();
                    try {
                        if (id > 0) {
                            taskManager.updateSubtask(subtask);
                            System.out.println("Subtask updated. ID = " + id);
                        } else {
                            System.out.println("Subtask created. ID = " + taskManager.addNewSubtask(subtask));
                        }
                        sendHttpStatus(exchange, 201);  // Created
                    } catch (TaskOverlapException exception) {
                        System.out.println(exception);
                        sendHttpStatus(exchange, 406);  // Not Acceptable
                    }
                } else {
                    super.handleDefaultPost(exchange);
                }
                break;
            default:
                System.out.println("/subtasks[id] path expected for GET/DELETE/POST method but " + requestPath
                        + " got for " + requestMethod + " method");
                super.handleDefaultGet(exchange);
        }
    }

    private void handleEpics(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        switch (requestMethod) {
            case "GET":
                if (Pattern.matches("^/epics$", requestPath)) {
                    handleGetTasks(exchange, EPIC);
                }  else if (Pattern.matches("^/epics/\\d+$", requestPath)) {
                    int pathId = parseIdFromPath(requestPath, "/epics/");
                    handleGetTaskById(exchange, EPIC, pathId);
                } else if (Pattern.matches("^/epics/\\d+/subtasks$", requestPath)) {
                    int pathId = parseIdFromPath(requestPath, "/epics/", "/subtasks");
                    String response = gson.toJson(taskManager.getEpicSubtasks(pathId));
                    sendText(exchange, response);  // OK
                } else {
                    super.handleDefaultGet(exchange);
                }
                break;
            case "DELETE":
                if (Pattern.matches("^/epics/\\d+$", requestPath)) {
                    int pathId =  parseIdFromPath(requestPath, "/epics/");
                    handleDeleteTask(exchange, EPIC, pathId);
                } else {
                    super.handleDefaultDelete(exchange);
                }
                break;
            case "POST":
                if (Pattern.matches("^/epics$", requestPath)) {
                    String json = readText(exchange);
                    if (json.isEmpty()) {
                        System.out.println("Body is empty for epic in POST request");
                        sendHttpStatus(exchange, 400);  // Bad Request
                        return;
                    }
                    final Epic epic = gson.fromJson(json, Epic.class);
                    System.out.println("Epic created. ID = " + taskManager.addNewEpic(epic));
                    sendHttpStatus(exchange, 201);   // Created
                } else {
                    super.handleDefaultPost(exchange);
                }
                break;
            default:
                System.out.println("/epics[id][/subtasks] path expected for GET/DELETE/POST method but " + requestPath
                        + " got for " + requestMethod + " method");
                super.handleDefaultGet(exchange);
        }
    }

    private void handleHistory(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        if (requestMethod.equals("GET")) {
            if (Pattern.matches("^/history$", requestPath)) {
                String response = gson.toJson(taskManager.getHistory());
                sendText(exchange, response);  // OK
            } else {
                super.handleDefaultGet(exchange);
            }
        } else {
            System.out.println("/history path expected for GET method but " + requestPath
                    + " got for " + requestMethod + " method");
            super.handleDefaultGet(exchange);
        }
    }

    private void handlePrioritized(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        if (requestMethod.equals("GET")) {
            if (Pattern.matches("^/prioritized$", requestPath)) {
                String response = gson.toJson(taskManager.getPrioritizedTasks());
                sendText(exchange, response);  // OK
            } else {
                super.handleDefaultGet(exchange);
            }
        } else {
            System.out.println("/prioritized path expected for GET method but " + requestPath
                    + " got for " + requestMethod + " method");
            super.handleDefaultGet(exchange);
        }
    }

    private void handleGetTasks(HttpExchange exchange, TaskType taskType) throws IOException {
        String response = "";
        switch (taskType) {
            case TASK:
                response = gson.toJson(taskManager.getTasks());
                break;
            case SUBTASK:
                response = gson.toJson(taskManager.getSubtasks());
                break;
            case EPIC:
                response = gson.toJson(taskManager.getEpics());
                break;
        }
        if (response.isEmpty() || response.equals("null")) {
            sendHttpStatus(exchange, 404);  // Not Found
        } else {
            sendText(exchange, response);  // OK
        }
    }

    private void handleGetTaskById(HttpExchange exchange, TaskType taskType, int taskId) throws IOException {
        if (taskId != -1) {
            Task task = switch (taskType) {
                case TASK -> taskManager.getTaskById(taskId);
                case SUBTASK -> taskManager.getSubtaskById(taskId);
                case EPIC -> taskManager.getEpicById(taskId);
            };
            if (task == null) {
                sendHttpStatus(exchange, 404);  // Not Found
            } else {
                sendText(exchange, gson.toJson(task));  // OK
            }
        } else {
            System.out.println("Incorrect ID '" + taskId + "' for " + taskType.name() + " GET in the URI path "
                    + exchange.getRequestURI().getPath());
            sendHttpStatus(exchange, 404);  // Not Found
        }
    }

    private void handleDeleteTask(HttpExchange exchange, TaskType taskType, int taskId) throws IOException {
        if (taskId != -1) {
            switch (taskType) {
                case TASK -> taskManager.deleteTask(taskId);
                case SUBTASK -> taskManager.deleteSubtask(taskId);
                case EPIC -> taskManager.deleteEpic(taskId);
            }
            System.out.println("Deleted " + taskType.name() + ". ID = " + taskId);
            sendHttpStatus(exchange, 200);  // OK
        } else {
            System.out.println("Incorrect ID '" + taskId + "' for DELETE in the URI path "
                    + exchange.getRequestURI().getPath());
            sendHttpStatus(exchange, 404);  // Not Found
        }
    }

    public void start() {
        System.out.println("TaskServer started on port " + PORT);
        System.out.println("Use http://localhost:" + PORT + "/tasks and other URIs to request services");
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("TaskServer stopped on port " + PORT);
    }

}
