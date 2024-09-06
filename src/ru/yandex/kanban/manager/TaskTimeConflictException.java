package ru.yandex.kanban.manager;

public class TaskTimeConflictException extends RuntimeException {
    public TaskTimeConflictException(String message) {
        super(message);
    }
}
