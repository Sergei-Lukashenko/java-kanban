package ru.yandex.kanban.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void shouldReturnEmptyHistoryListWhenNoTasks() {
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "History is not null.");
        assertTrue(history.isEmpty(), "History must be empty.");
    }

    @Test
    void shouldKeepHistoryAfterAddingAnItem() {
        Task task = new Task("Task title", "Task description");
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "History is not null.");
        assertEquals(1, history.size(), "History is not empty.");
    }

    @Test
    void shouldKeepTheSingleTaskAfterRepetitiveAddition() {
        Task task = new Task("Task title", "Task description");
        historyManager.add(task);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        Task taskFromHist = history.get(0);
        assertEquals(task, taskFromHist, "History will keep the single task after repetitive addition.");
        assertEquals(1, history.size(), "History length after repetitive addition must be = 1.");
    }

}