package ru.yandex.kanban.manager;

public class TaskOverlapException extends RuntimeException {
    public TaskOverlapException(String message) {
        super(message);
    }
}
