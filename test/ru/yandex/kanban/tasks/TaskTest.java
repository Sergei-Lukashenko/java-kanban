package ru.yandex.kanban.tasks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.manager.Managers;
import ru.yandex.kanban.manager.TaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    private static TaskManager manager;

    @BeforeAll
    public static void beforeAll() {
        manager = Managers.getDefault();
    }

    @Test
    public void similarTasksWithTheSameIdShouldBeEqual () {
        Task task1 = new Task("Task title", "Task description");
        final int id1 = manager.addNewTask(task1);
        Task task2 = new Task("Task title", "Task description");
        task2.setId(id1);
        assertEquals(task1, task2);
    }

}