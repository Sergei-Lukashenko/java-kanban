package ru.yandex.kanban.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.tasks.Task;
import ru.yandex.kanban.tasks.TaskStatus;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private static FileBackedTaskManager manager;
    private static File file;

    @BeforeAll
    public static void beforeAll() {
        try {
            file = File.createTempFile("java-kanban", null, null);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        manager = Managers.getFileDefault(file.getPath());
    }

    @Test
    void shouldSaveAndRestoreEmptyFile() {
        manager.deleteAllTasks();

        manager.save();

        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(newManager.getTasks().isEmpty(), "Task list is empty");
        assertTrue(newManager.getEpics().isEmpty(), "Epic list is empty");
        assertTrue(newManager.getSubtasks().isEmpty(), "Subtask list is empty");
    }

    @Test
    void shouldSaveAndRestoreMultipleTasksInFile() {
        Task task1 = new Task("Task #1", "Task1 description");
        Task task2 = new Task("Task #2", "Task2 description");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        int taskId1 = manager.addNewTask(task1);
        int taskId2 = manager.addNewTask(task2);

        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(file);
        Task fileTask1 = newManager.getTaskById(taskId1);
        Task fileTask2 = newManager.getTaskById(taskId2);

        assertEquals(task1, fileTask1);
        assertEquals(task2, fileTask2);
    }

}
