package ru.yandex.kanban.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.tasks.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private static FileBackedTaskManager manager;
    private static File file;

    @BeforeEach
    public void beforeEach() {
        try {
            file = File.createTempFile("java-kanban", null, null);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        manager = new FileBackedTaskManager(file);
    }

    @Test
    void shouldSaveAndRestoreEmptyFile() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();

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

        assertEquals(task1, fileTask1, "Task #1 restored from file correctly" );
        assertEquals(task2, fileTask2, "Task #2 restored from file correctly");
    }

    @Test
    void shouldCorrectlySaveAndRestoreEpicReferenceForSubtask() {
        Epic epic = new Epic("Epic #1", "Epic #1 description");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Subtask #1", "Subtask #1 description", epicId);
        int subtaskId = manager.addNewSubtask(subtask);

        FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(file);
        Epic fileEpic = newManager.getEpicById(epicId);
        Subtask fileSubtask = newManager.getSubtaskById(subtaskId);

        assertEquals(epicId, fileEpic.getId(),"Epic ID restored from file correctly");
        assertEquals(subtaskId, fileSubtask.getId(),"Subtask ID restored from file correctly");
        assertEquals(epicId, fileSubtask.getEpicId(),"Epic ID for subtask restored from file correctly");
    }
}
