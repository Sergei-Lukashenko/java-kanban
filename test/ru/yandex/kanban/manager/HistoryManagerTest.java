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

    private static HistoryManager historyManager;

    Task task = new Task("Task title", "Task description");

    @BeforeAll
    public static void beforeAll() {
        historyManager = Managers.getDefaultHistory();
    }

    @BeforeEach
    public void beforeEach() { historyManager.clear();  }

    @Test
    void shouldReturnEmptyHistoryListWhenNoTasks() {
        final List<Task> expectedHistory = historyManager.getHistory();
        assertNotNull(expectedHistory, "History is null.");
        assertTrue(expectedHistory.isEmpty(), "History must be empty.");
    }

    @Test
    void shouldKeepHistoryAfterAddingAnItem() {
        historyManager.add(task);
        final List<Task> expectedHistory = historyManager.getHistory();
        assertNotNull(expectedHistory, "History is null after adding one task.");
        assertEquals(1, expectedHistory.size(), "History length != 1 while 1 task expected there.");
    }

    @Test
    void shouldKeepTheSingleTaskAfterRepetitiveAddition() {
        historyManager.add(task);
        historyManager.add(task);
        final List<Task> expectedHistory = historyManager.getHistory();
        Task expectedTaskFromHist = expectedHistory.getFirst();
        assertEquals(task, expectedTaskFromHist, "History must keep the single added task after repetitive addition.");
        assertEquals(1, expectedHistory.size(), "History length after repetitive addition must be = 1.");
    }

    @Test
    void shouldRemoveTheTaskCorrectly() {
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        Task taskFromHist = history.getFirst();
        historyManager.remove(taskFromHist.getId());
        List<Task> expectedHistory = historyManager.getHistory();
        assertNotNull(expectedHistory, "History is  null after deleting a single added task.");
        assertEquals(0, expectedHistory.size(), "History length after addition/removal must be = 0.");
    }

}