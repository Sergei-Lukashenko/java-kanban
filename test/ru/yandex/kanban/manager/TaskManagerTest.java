package ru.yandex.kanban.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private static TaskManager manager;

    @BeforeAll
    public static void beforeAll() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldReturnTheSameTaskAfterAddingNewTask() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();

        Task task = new Task("Task title", "Task description");
        final int id = manager.addNewTask(task);
        Task actualTask = manager.getTaskById(id);
        assertEquals(task, actualTask, "Added task is not equal to the returned one");

        final List<Task> actualTasks = manager.getTasks();
        assertNotNull(actualTasks, "No tasks returned.");
        assertEquals(1, actualTasks.size(), "Wrong size of task list.");
        assertEquals(task, actualTasks.getFirst(), "Added task is not equal to the task in the list.");
    }

    @Test
    void shouldReturnTheSameEpicAfterAddingNewEpic() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();

        Epic expectedEpic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(expectedEpic);
        Epic actualEpic = manager.getEpicById(id);
        assertEquals(expectedEpic, actualEpic, "Added epic is not equal to the returned one");

        final List<Epic> actualEpics = manager.getEpics();
        assertNotNull(actualEpics, "No epics returned.");
        assertEquals(1, actualEpics.size(), "Wrong size of epic list.");
        assertEquals(expectedEpic, actualEpics.getFirst(), "Added epic is not equal to the epic in the list.");
    }

    @Test
    void shouldReturnTheSameSubtaskAfterAddingNewSubtask() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();

        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Subtask expectedSubtask = new Subtask("Subtask title", "Subtask description", id);
        final int subtaskId = manager.addNewSubtask(expectedSubtask);
        Subtask actualSubtask = manager.getSubtaskById(subtaskId);
        assertEquals(expectedSubtask, actualSubtask, "Added subtask is not equal to the returned one");

        final List<Subtask> actualSubtasks = manager.getSubtasks();
        assertNotNull(actualSubtasks, "No subtasks returned.");
        assertEquals(1, actualSubtasks.size(), "Wrong size of subtask list.");
        assertEquals(expectedSubtask, actualSubtasks.getFirst(), "Added subtask is not equal to the subtask in the list.");
    }

    @Test
    void shouldDeleteSubtaskFromEpicAfterDeletingSubtask() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();

        Epic epic = new Epic("Epic title", "Epic description");
        final int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask #1", "Subtask1 description", epicId);
        Subtask subtask2 = new Subtask("Subtask #2", "Subtask2 description", epicId);

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        final int expectedSubtask1Id = subtask1.getId();
        final int subtask2Id = subtask2.getId();

        manager.deleteSubtask(subtask2Id);
        for (int actualSubtaskId : epic.getSubtaskIds()) {
            assertEquals(expectedSubtask1Id, actualSubtaskId, "Only subtask #1 must be kept in Epic");
        }
    }

    @Test
    void shouldReturnHistoryListWhenNoTasks() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.clear();

        final List<Task> actualHistory = manager.getHistory();

        assertNotNull(actualHistory, "Manager must return  history on start.");
        assertTrue(actualHistory.isEmpty(), "History must be empty on start.");
    }

    @Test
    void shouldReturnOneItemHistoryAfterAddingOneTask() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.clear();
        manager.deleteAllTasks();

        Task task = new Task("Task title", "Task description");
        final int expectedId = manager.addNewTask(task);
        assertEquals(expectedId, manager.getTaskById(expectedId).getId(), "Task id stored in manager is incorrect");

        final List<Task> actualHistory = manager.getHistory();

        assertNotNull(actualHistory, "Manager must return history after adding 1 task.");
        assertEquals(1, actualHistory.size(), "History size must be = 1.");
    }

    @Test
    void addedTasksArePrioritizedByTime() {
        manager.deleteAllTasks();
        Task laterTask = new Task("Late task title", "Late task description");
        laterTask.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        Task earlierTask = new Task("Early task  title", "Early task  description");
        earlierTask.setStartTime(LocalDateTime.of(2024, 9, 7, 0, 37));
        int laterExpectedId = manager.addNewTask(laterTask);
        int earlierExpectedId = manager.addNewTask(earlierTask);
        List<Task> actualTasksByTime = manager.getPrioritizedTasks();
        assertEquals(earlierExpectedId, actualTasksByTime.get(0).getId(), "ID of earlier task should be first" );
        assertEquals(laterExpectedId, actualTasksByTime.get(1).getId(), "ID of later task should be next" );
    }

    @Test
    void intersectedTaskIsRefusedOnAdding() {
        manager.deleteAllTasks();
        LocalDateTime initialStart = LocalDateTime.now();
        Task task = new Task("Task title", "Task description");
        task.setStartTime(initialStart);
        task.setDuration(Duration.ofMinutes(100));
        Task taskInter = new Task("Intersected task title", "Intersected task description");
        taskInter.setStartTime(initialStart.plusMinutes(10));
        taskInter.setDuration(Duration.ofMinutes(120));
        int expectedTaskId = manager.addNewTask(task);
        try {
            manager.addNewTask(taskInter);
        } catch (TaskOverlapException ignored) {
        }
        List<Task> actualTasksByTime = manager.getPrioritizedTasks();
        assertEquals(1, actualTasksByTime.size(), "Only 1st of 2 intersected tasks should be added");
        assertEquals(expectedTaskId, actualTasksByTime.getFirst().getId(), "ID of the 1st task must be got" );
    }

    @Test
    void shouldReturnEpicSubtasksInTheSameOrderButSubtasksByTimeFromEarlierToLater() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();

        Epic epic = new Epic("Epic title", "Epic description");
        final int epicId = manager.addNewEpic(epic);

        Subtask expectedSubtask1 = new Subtask("Later subtask #1", "Later subtask1 description", epicId);
        expectedSubtask1.setStartTime(LocalDateTime.now().plusDays(1));
        expectedSubtask1.setDuration(Duration.ofDays(2));
        Subtask expectedSubtask2 = new Subtask("Earlier subtask #2", "Earlier subtask2 description", epicId);
        expectedSubtask2.setStartTime(LocalDateTime.now());
        expectedSubtask2.setDuration(Duration.ofMinutes(180));

        manager.addNewSubtask(expectedSubtask1);
        manager.addNewSubtask(expectedSubtask2);

        List<Subtask> actualSubtasks = manager.getEpicSubtasks(epicId);
        assertEquals(2, actualSubtasks.size(), "2 subtasks are in the epic");
        assertEquals(expectedSubtask1, actualSubtasks.get(0), "1st added subtask should be in place");
        assertEquals(expectedSubtask2, actualSubtasks.get(1), "2nd added subtask should be in place");

        List<Task> actualTasksByTime = manager.getPrioritizedTasks();
        assertEquals(expectedSubtask2, actualTasksByTime.get(0), "Earlier subtask must be the first in tasksByTime");
        assertEquals(expectedSubtask1, actualTasksByTime.get(1), "Later subtask must be the second in tasksByTime");
    }
}