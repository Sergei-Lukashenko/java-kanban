package ru.yandex.kanban.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoryManagerTest {

    private static TaskManager manager;
    private static HistoryManager historyManager;

    @BeforeAll
    public static void beforeAll() {
        manager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
    }

    @BeforeEach
    public void beforeEach() { historyManager.clear();  }

    @Test
    void shouldKeepHistoryAfterAddingAnItem() {
        Task task = new Task("Task title", "Task description");
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "History is not null.");
        assertEquals(1, history.size(), "History is not empty.");
    }

    @Test
    void shouldNotChangeTheTaskAfterRepetitiveAddition() {
        Task task = new Task("Task title", "Task description");
        historyManager.add(task);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        Task task1 = history.get(0);
        Task task2 = history.get(1);
        assertEquals(task1, task2, "History could keep the same tasks.");
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