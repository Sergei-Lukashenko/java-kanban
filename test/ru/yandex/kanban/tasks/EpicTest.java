package ru.yandex.kanban.tasks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import ru.yandex.kanban.manager.Managers;
import ru.yandex.kanban.manager.TaskManager;

import java.time.LocalDateTime;
import java.util.ArrayList;

class EpicTest {

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
    public void shouldReturnAddedSubtaskInGetSubtaskIds() {
        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Subtask title", "Subtask description", id);
        final int subtaskId = manager.addNewSubtask(subtask);
        for (Integer addedSubtaskId : epic.getSubtaskIds()) {
            assertEquals(subtaskId, addedSubtaskId);
            assertEquals(subtask, manager.getSubtaskById(addedSubtaskId));
        }
    }

    @Test
    public void avoidAdding2SubtasksWithTheSameId() {
        Epic epic = new Epic("Epic title", "Epic description");
        manager.addNewEpic(epic);
        epic.addSubtaskId(1);
        epic.addSubtaskId(1);
        ArrayList<Integer> subtasks = epic.getSubtaskIds();
        assertEquals(1, subtasks.size());
    }

    @Test
    public void similarEpicsWithTheSameIdShouldBeEqual() {
        LocalDateTime sameMoment = LocalDateTime.now();
        Epic epic1 = new Epic("Epic title", "Epic description");
        epic1.setStartTime(sameMoment);
        final int sameId = manager.addNewEpic(epic1);
        Epic epic2 = new Epic(epic1);
        manager.addNewEpic(epic2);
        epic2.setStartTime(sameMoment);
        epic2.setId(sameId);
        assertEquals(epic1, epic2);
    }

    @Test
    public void cleanSubtaskIdsShouldRemoveAllSubtasks() {
        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1 title", "Subtask 1 description", id);
        manager.addNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2 title", "Subtask 2 description", id);
        manager.addNewSubtask(subtask2);
        ArrayList<Integer> subtasks = epic.getSubtaskIds();
        assertEquals(2, subtasks.size());
        epic.cleanSubtaskIds();
        assertEquals(0, subtasks.size());
    }

    @Test
    public void removeSubtaskIdShouldRemoveTheSubtaskById() {
        Epic epic = new Epic("Epic title", "Epic description");
        final int id = manager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1 title", "Subtask 1 description", id);
        manager.addNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2 title", "Subtask 2 description", id);
        manager.addNewSubtask(subtask2);
        int idToRemove = subtask2.getId();
        epic.removeSubtaskId(idToRemove);
        for (Integer everyId : epic.getSubtaskIds()) {
            assertNotEquals(idToRemove, everyId);
        }
    }
}