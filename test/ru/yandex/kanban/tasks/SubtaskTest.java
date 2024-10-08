package ru.yandex.kanban.tasks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.manager.Managers;
import ru.yandex.kanban.manager.TaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {

    private static TaskManager manager;

    @BeforeAll
    public static void beforeAll() {
        manager = Managers.getDefault();
    }

    @BeforeEach
    void init() {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
    }

    @Test
    public void similarSubtasksWithTheSameIdShouldBeEqual() {
        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Subtask similar title", "Subtask similar description", id);
        manager.addNewSubtask(subtask1);
        int sameId = subtask1.getId();
        Subtask subtask2 = new Subtask(subtask1);
        manager.addNewSubtask(subtask2);
        subtask2.setId(sameId);
        assertEquals(subtask1, subtask2);
    }

    @Test
    public void subtasksOfTheSameEpicShouldHaveTheSameEpicId() {
        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Subtask similar title", "Subtask similar description", id);
        manager.addNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask similar title", "Subtask similar description", id);
        manager.addNewSubtask(subtask1);
        int epicIdFromSubtask1 = subtask1.getEpicId();
        int epicIdFromSubtask2 = subtask2.getEpicId();
        assertEquals(epicIdFromSubtask1, epicIdFromSubtask2);
    }
}