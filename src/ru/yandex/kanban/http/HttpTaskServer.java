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

    // fields initialized in getEndpoint and used in handle* methods
    private int pathId;  // -1 for incorrect ID specified in URI path
    private TaskType taskType;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        gson = Managers.getGson();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", this::handleTasks);
        server.createContext("/subtasks", this::handleTasks);
        server.createContext("/epics", this::handleTasks);
        server.createContext("/history", this::handleTasks);
        server.createContext("/prioritized", this::handleTasks);
    }

    private void handleTasks(HttpExchange exchange) {
        try {
            Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
            if (endpoint == Endpoint.UNKNOWN) {
                sendHttpStatus(exchange, 405);  // Method Not Allowed
                return;
            }
            switch (endpoint) {
                case GET_TASKS:
                case GET_SUBTASKS:
                case GET_EPICS:
                    handleGetTasks(exchange);
                    break;

                case GET_TASK_BY_ID:
                case GET_SUBTASK_BY_ID:
                case GET_EPIC_BY_ID:
                    handleGetTask(exchange);
                    break;

                case GET_EPIC_SUBTASKS: {
                    String response = gson.toJson(taskManager.getEpicSubtasks(pathId));
                    sendText(exchange, response);  // OK
                    break;
                }

                case GET_HISTORY: {
                    String response = gson.toJson(taskManager.getHistory());
                    sendText(exchange, response);  // OK
                    break;
                }

                case GET_PRIORITIZED: {
                    String response = gson.toJson(taskManager.getPrioritizedTasks());
                    sendText(exchange, response);  // OK
                    break;
                }

                case DELETE_TASK:
                case DELETE_SUBTASK:
                case DELETE_EPIC:
                    handleDeleteTask(exchange);
                    break;

                case POST_TASK:
                case POST_SUBTASK:
                case POST_EPIC:
                    handlePostTask(exchange);
                    break;
            }
        } catch (Exception exception) {
            System.out.println(exception);
        } finally {
            exchange.close();
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        String response = gson.toJson( switch (taskType) {
            case TASK -> taskManager.getTasks();
            case SUBTASK -> taskManager.getSubtasks();
            case EPIC -> taskManager.getEpics();
        });
        if (response.equals("null")) {
            sendHttpStatus(exchange, 404);  // Not Found
        } else {
            sendText(exchange, response);  // OK
        }
    }

    private void handleGetTask(HttpExchange exchange) throws IOException {
        if (pathId != -1) {
            Task task = switch (taskType) {
                case TASK -> taskManager.getTaskById(pathId);
                case SUBTASK -> taskManager.getSubtaskById(pathId);
                case EPIC -> taskManager.getEpicById(pathId);
            };
            if (task == null) {
                sendHttpStatus(exchange, 404);  // Not Found
            } else {
                sendText(exchange, gson.toJson(task));  // OK
            }
        } else {
            System.out.println("Incorrect ID '" + pathId + "' specified in the URI path "
                    + exchange.getRequestURI().getPath());
            sendHttpStatus(exchange, 404);  // Not Found
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        if (pathId != -1) {
            switch (taskType) {
                case TASK -> taskManager.deleteTask(pathId);
                case SUBTASK -> taskManager.deleteSubtask(pathId);
                case EPIC -> taskManager.deleteEpic(pathId);
            }
            sendHttpStatus(exchange, 200);  // OK
        } else {
            System.out.println("Incorrect ID '" + pathId + "' specified in the URI path "
                    + exchange.getRequestURI().getPath());
            sendHttpStatus(exchange, 404);  // Not Found
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String json = readText(exchange);
        if (json.isEmpty()) {
            System.out.println("Body is empty for task in POST request");
            sendHttpStatus(exchange, 400);  // Bad Request
            return;
        }
        if (taskType == TASK) {
            final Task task = gson.fromJson(json, Task.class);
            final int id = task.getId();
            try {
                if (id > 0) {
                    taskManager.updateTask(task);
                    System.out.println("Task updated. ID = " + id);
                    sendHttpStatus(exchange, 201);  // Created
                } else {
                    System.out.println("Task created. ID = " + taskManager.addNewTask(task));
                    sendHttpStatus(exchange, 201);  // Created
                }
            } catch (TaskOverlapException exeption) {
                System.out.println(exeption);
                sendHttpStatus(exchange, 406);  // Not Acceptable
            }
        } else if (taskType == SUBTASK) {
            final Subtask subtask = gson.fromJson(json, Subtask.class);
            final int id = subtask.getId();
            try {
                if (id > 0) {
                    taskManager.updateSubtask(subtask);
                    System.out.println("Subtask updated. ID = " + id);
                    sendHttpStatus(exchange, 201);  // Created
                } else {
                    System.out.println("Subtask created. ID = " + taskManager.addNewSubtask(subtask));
                    sendHttpStatus(exchange, 201);  // Created
                }
            } catch (TaskOverlapException exeption) {
                System.out.println(exeption);
                sendHttpStatus(exchange, 406);  // Not Acceptable
            }
        } else if (taskType == EPIC) {
            final Epic epic = gson.fromJson(json, Epic.class);
            System.out.println("Epic created. ID = " + taskManager.addNewEpic(epic));
            sendHttpStatus(exchange, 201);   // Created
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

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        switch (requestMethod) {
            case "GET":
                if (Pattern.matches("^/tasks$", requestPath)) {
                    taskType = TASK;
                    return Endpoint.GET_TASKS;
                } else if (Pattern.matches("^/tasks/\\d+$", requestPath)) {
                    taskType = TASK;
                    pathId =  parseIdFromPath(requestPath, "/tasks/");
                    return Endpoint.GET_TASK_BY_ID;
                } else if (Pattern.matches("^/subtasks$", requestPath)) {
                    taskType = SUBTASK;
                    return Endpoint.GET_SUBTASKS;
                } else if (Pattern.matches("^/subtasks/\\d+$", requestPath)) {
                    taskType = SUBTASK;
                    pathId =  parseIdFromPath(requestPath, "/subtasks/");
                    return Endpoint.GET_SUBTASK_BY_ID;
                } else if (Pattern.matches("^/epics$", requestPath)) {
                    taskType = EPIC;
                    return Endpoint.GET_EPICS;
                } else if (Pattern.matches("^/epics/\\d+$", requestPath)) {
                    taskType = EPIC;
                    pathId =  parseIdFromPath(requestPath, "/epics/");
                    return Endpoint.GET_EPIC_BY_ID;
                } else if (Pattern.matches("^/epics/\\d+/subtasks$", requestPath)) {
                    pathId =  parseIdFromPath(requestPath, "/epics/", "/subtasks");
                    return Endpoint.GET_EPIC_SUBTASKS;
                } else if (Pattern.matches("^/history$", requestPath)) {
                    return Endpoint.GET_HISTORY;
                } else if (Pattern.matches("^/prioritized$", requestPath)) {
                    return Endpoint.GET_PRIORITIZED;
                } else {
                    System.out.println("/tasks[id], /subtasks[id], /epics[id], /epics/{id}/subtasks, /history "
                            + "or /prioritized path expected for GET method but " + requestPath + " got");
                    return Endpoint.UNKNOWN;
                }

            case "DELETE":
                if (Pattern.matches("^/tasks/\\d+$", requestPath)) {
                    taskType = TASK;
                    pathId =  parseIdFromPath(requestPath, "/tasks/");
                    return Endpoint.DELETE_TASK;
                } else if (Pattern.matches("^/subtasks/\\d+$", requestPath)) {
                    taskType = SUBTASK;
                    pathId =  parseIdFromPath(requestPath, "/subtasks/");
                    return Endpoint.DELETE_SUBTASK;
                } else if (Pattern.matches("^/epics/\\d+$", requestPath)) {
                    taskType = EPIC;
                    pathId =  parseIdFromPath(requestPath, "/epics/");
                    return Endpoint.DELETE_EPIC;
                } else {
                    System.out.println("/tasks{id}, /subtasks{id} or /epics{id} path expected for DELETE method but "
                            + requestPath + " got");
                    return Endpoint.UNKNOWN;
                }

            case "POST":
                if (Pattern.matches("^/tasks$", requestPath)) {
                    taskType = TASK;
                    return Endpoint.POST_TASK;
                } else if (Pattern.matches("^/subtasks$", requestPath)) {
                    taskType = SUBTASK;
                    return Endpoint.POST_SUBTASK;
                } else if (Pattern.matches("^/epics$", requestPath)) {
                    taskType = EPIC;
                    return Endpoint.POST_EPIC;
                } else {
                    System.out.println("/tasks, /subtasks or /epics path expected for POST method but "
                            + requestPath + " got");
                    return Endpoint.UNKNOWN;
                }

            default:
                System.out.println("GET, POST or DELETE method expected but " + requestMethod + " got");
                return Endpoint.UNKNOWN;
        }
    }

    private int parseIdFromPath(String path, String prefixToRemove) {
        return parseIdFromPath(path, prefixToRemove, "");
    }

    private int parseIdFromPath(String path, String prefixToRemove, String postfixToRemove) {
        String pathId = path.replaceFirst(prefixToRemove, "");
        if (!postfixToRemove.isEmpty()) {
            pathId = pathId.replaceFirst(postfixToRemove, "");
        }
        try {
            return Integer.parseInt(pathId);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    enum Endpoint {
        GET_TASKS, GET_TASK_BY_ID, POST_TASK, DELETE_TASK,
        GET_SUBTASKS, GET_SUBTASK_BY_ID, POST_SUBTASK, DELETE_SUBTASK,
        GET_EPICS, GET_EPIC_BY_ID, GET_EPIC_SUBTASKS, POST_EPIC, DELETE_EPIC,
        GET_HISTORY, GET_PRIORITIZED, UNKNOWN
    }
}
