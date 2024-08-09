package ru.yandex.kanban.manager;

public final class Managers {

    private static TaskManager DEFAULT_MANAGER;
    private static HistoryManager DEFAULT_HISTORY;

    private Managers() {
        throw new RuntimeException("Utility class Managers cannot be implemented, call Managers.getDefault*() instead");
    }

    public static TaskManager getDefault() {
        if (DEFAULT_MANAGER == null) {
            DEFAULT_MANAGER = new InMemoryTaskManager();
        }
        return DEFAULT_MANAGER;
    }

    public static HistoryManager getDefaultHistory() {
        if (DEFAULT_HISTORY == null) {
            DEFAULT_HISTORY = new InMemoryHistoryManager();
        }
        return DEFAULT_HISTORY;
    }
}
