package ru.yandex.kanban.tasks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.kanban.manager.Managers;
import ru.yandex.kanban.manager.TaskManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    private static TaskManager manager;

    @BeforeAll
    public static void beforeAll() {
        manager = Managers.getDefault();
    }

    @Test
    public void similarTasksWithTheSameIdShouldBeEqual () {
        LocalDateTime sameMoment = LocalDateTime.now();
        Task task1 = new Task("Task title", "Task description");
        task1.setStartTime(sameMoment);
        final int sameId = manager.addNewTask(task1);
        Task task2 = new Task(task1);
        manager.addNewTask(task2);
        task2.setId(sameId);
        task2.setStartTime(sameMoment);
        assertEquals(task1, task2);
    }

}