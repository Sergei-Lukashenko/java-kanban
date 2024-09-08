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
        Task returnedTask = manager.getTaskById(id);
        assertEquals(task, returnedTask, "Added task is not equal to the returned one");

        final List<Task> tasks = manager.getTasks();
        assertNotNull(tasks, "No tasks returned.");
        assertEquals(1, tasks.size(), "Wrong size of task list.");
        assertEquals(task, tasks.getFirst(), "Added task is not equal to the task in the list.");
    }

    @Test
    void shouldReturnTheSameEpicAfterAddingNewEpic() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();

        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Epic returnedEpic = manager.getEpicById(id);
        assertEquals(epic, returnedEpic, "Added epic is not equal to the returned one");

        final List<Epic> epics = manager.getEpics();
        assertNotNull(epics, "No epics returned.");
        assertEquals(1, epics.size(), "Wrong size of epic list.");
        assertEquals(epic, epics.getFirst(), "Added epic is not equal to the epic in the list.");
    }

    @Test
    void shouldReturnTheSameSubtaskAfterAddingNewSubtask() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();

        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Subtask title", "Subtask description", id);
        final int subtaskId = manager.addNewSubtask(subtask);
        Subtask returnedSubtask = manager.getSubtaskById(subtaskId);
        assertEquals(subtask, returnedSubtask, "Added subtask is not equal to the returned one");

        final List<Subtask> subtasks = manager.getSubtasks();
        assertNotNull(subtasks, "No subtasks returned.");
        assertEquals(1, subtasks.size(), "Wrong size of subtask list.");
        assertEquals(subtask, subtasks.getFirst(), "Added subtask is not equal to the subtask in the list.");
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

        final int subtask1Id = subtask1.getId();
        final int subtask2Id = subtask2.getId();

        manager.deleteSubtask(subtask2Id);
        for (int subtaskId : epic.getSubtaskIds()) {
            assertEquals(subtask1Id, subtaskId, "Only subtask1 is kept in Epic");
        }
    }

    @Test
    void shouldReturnHistoryListWhenNoTasks() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.clear();

        final List<Task> history = manager.getHistory();

        assertNotNull(history, "Manager must return  history on start.");
        assertTrue(history.isEmpty(), "History is empty on start.");
    }

    @Test
    void shouldReturnOneItemHistoryAfterAddingOneTask() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.clear();
        manager.deleteAllTasks();

        Task task = new Task("Task title", "Task description");
        final int id = manager.addNewTask(task);
        assertEquals(id, manager.getTaskById(id).getId(), "Task id stored in manager correctly");

        final List<Task> history = manager.getHistory();

        assertNotNull(history, "Manager must return history after adding 1 task.");
        assertEquals(1, history.size(), "History size must be = 1.");
    }

    @Test
    void addedTasksArePrioritizedByTime() {
        manager.deleteAllTasks();
        Task laterTask = new Task("Late task title", "Late task description");
        laterTask.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0));
        Task earlierTask = new Task("Early task  title", "Early task  description");
        earlierTask.setStartTime(LocalDateTime.of(2024, 9, 7, 0, 37));
        int laterId = manager.addNewTask(laterTask);
        int earlierId = manager.addNewTask(earlierTask);
        List<Task> tasksByTime = manager.getPrioritizedTasks();
        assertEquals(earlierId, tasksByTime.get(0).getId(), "ID of earlier task first" );
        assertEquals(laterId, tasksByTime.get(1).getId(), "ID of later task first" );
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
        int taskId = manager.addNewTask(task);
        try {
            manager.addNewTask(taskInter);
        } catch (TaskOverlapException ignored) {
        }
        List<Task> tasksByTime = manager.getPrioritizedTasks();
        assertEquals(1, tasksByTime.size(), "Only 1st of 2 intersected tasks was added");
        assertEquals(taskId, tasksByTime.getFirst().getId(), "ID of the 1st task got" );
    }

    @Test
    void shouldReturnEpicSubtasksInTheSameOrderButSubtasksByTimeFromEarlierToLater() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();

        Epic epic = new Epic("Epic title", "Epic description");
        final int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Later subtask #1", "Later subtask1 description", epicId);
        subtask1.setStartTime(LocalDateTime.now().plusDays(1));
        subtask1.setDuration(Duration.ofDays(2));
        Subtask subtask2 = new Subtask("Earlier subtask #2", "Earlier subtask2 description", epicId);
        subtask2.setStartTime(LocalDateTime.now());
        subtask2.setDuration(Duration.ofMinutes(180));

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        List<Subtask> subtasks = manager.getEpicSubtasks(epicId);
        assertEquals(2, subtasks.size(), "2 subtasks are in the epic");
        assertEquals(subtask1, subtasks.get(0), "1st added subtask is in place");
        assertEquals(subtask2, subtasks.get(1), "2nd added subtask is in place");

        List<Task> tasksByTime = manager.getPrioritizedTasks();
        assertEquals(subtask2, tasksByTime.get(0), "Earlier subtask is the first in tasksByTime");
        assertEquals(subtask1, tasksByTime.get(1), "Later subtask is the second in tasksByTime");
    }
}