package ru.yandex.kanban.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskManagerTest {
    private static TaskManager manager;

    @BeforeAll
    public static void beforeAll() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldReturnLimitedHistoryWhenHistoryLimitExceededForInMemoryHistoryManager() {
        manager.deleteAllTasks();

        final int maxHistoryLen = InMemoryHistoryManager.MAX_HISTORY_LEN;
        final int tasksNumber = maxHistoryLen + 2;
        for (int i = 0; i < tasksNumber; i++) {  // steps number > maxHistoryLen
            Task task = new Task("Task title " + i, "Task description");
            manager.addNewTask(task);
        }
        assertEquals(tasksNumber, manager.getTasks().size(),
                "Task manager returns task number greater than history length");
        for (Task t : manager.getTasks()) {  // get all the tasks one-by-one with manager.getTask()
            int id = t.getId();
            manager.getTask(id);
        }
        List<Task> histList = manager.getHistory();
        assertEquals(maxHistoryLen, histList.size(), "Task manager returns limited history");
    }

    @Test
    void shouldReturnTheSameTaskAfterAddingNewTask() {
        manager.deleteAllTasks();

        Task task = new Task("Task title", "Task description");
        final int id = manager.addNewTask(task);
        Task returnedTask = manager.getTask(id);
        assertEquals(task, returnedTask, "Added task is not equal to the returned one");

        final List<Task> tasks = manager.getTasks();
        assertNotNull(tasks, "No tasks returned.");
        assertEquals(1, tasks.size(), "Wrong size of task list.");
        assertEquals(task, tasks.getFirst(), "Added task is not equal to the task in the list.");
    }

    @Test
    void shouldReturnTheSameEpicAfterAddingNewEpic() {
        manager.deleteAllEpics();

        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Epic returnedEpic = manager.getEpic(id);
        assertEquals(epic, returnedEpic, "Added epic is not equal to the returned one");

        final List<Epic> epics = manager.getEpics();
        assertNotNull(epics, "No epics returned.");
        assertEquals(1, epics.size(), "Wrong size of epic list.");
        assertEquals(epic, epics.getFirst(), "Added epic is not equal to the epic in the list.");
    }

    @Test
    void shouldReturnTheSameSubtaskAfterAddingNewSubtask() {
        manager.deleteAllEpics();

        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Subtask title", "Subtask description", id);
        final int subtaskId = manager.addNewSubtask(subtask);
        Subtask returnedSubtask = manager.getSubtask(subtaskId);
        assertEquals(subtask, returnedSubtask, "Added subtask is not equal to the returned one");

        final List<Subtask> subtasks = manager.getSubtasks();
        assertNotNull(subtasks, "No subtasks returned.");
        assertEquals(1, subtasks.size(), "Wrong size of subtask list.");
        assertEquals(subtask, subtasks.getFirst(), "Added subtask is not equal to the subtask in the list.");
    }
}