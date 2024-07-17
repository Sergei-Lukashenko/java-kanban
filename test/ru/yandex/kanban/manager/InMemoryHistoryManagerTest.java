package ru.yandex.kanban.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static TaskManager manager;
    private static HistoryManager historyManager;

    @BeforeAll
    public static void beforeAll() {
        manager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void shouldKeepHistoryForSpecifiedMaximumLengthAndRotateProperly() {
        int maxHistoryLength = 0;
        if (historyManager instanceof InMemoryHistoryManager) {
            maxHistoryLength = InMemoryHistoryManager.MAX_HISTORY_LEN;
        }
        for (int i = 1; i <= maxHistoryLength ; i++) {
            Task task = new Task("Task title #" + i, "Task description");
            manager.addNewTask(task);
            historyManager.add(task);
        }

        Task task = new Task("Task title #" + (maxHistoryLength + 1), "Task description");
        manager.addNewTask(task);
        historyManager.add(task);

        int seqNo = 2;   // first item in history must be rotated and removed from history
        for (Task t : historyManager.getHistory()) {
            assertEquals("Task title #" + seqNo, t.getTitle());
            seqNo++;
        }
    }

}