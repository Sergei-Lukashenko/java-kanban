package ru.yandex.kanban.manager;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(String message, Exception exception) {
        super(message, exception);
    }
}
