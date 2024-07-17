package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.Task;

import java.util.LinkedList;
import java.util.List;

class InMemoryHistoryManager implements HistoryManager {

    public final static int MAX_HISTORY_LEN = 10;

    private final LinkedList<Task> history = new LinkedList<>();

    InMemoryHistoryManager() {}  // empty package-private constructor to avoid cross-package access,
                                 // see also Managers.getDefaultHistory()

    public void add(Task task) {
        if (history.size() >= MAX_HISTORY_LEN) {
            history.removeFirst();
        }
        history.add(task);
    }

    public List<Task> getHistory() {
        return history;
    }
}
