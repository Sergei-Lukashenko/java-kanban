package ru.yandex.kanban.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    public void defaultTaskManagerIsNotNull() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager);
    }

    @Test
    public void defaultHistoryManagerIsNotNull() {
        HistoryManager history = Managers.getDefaultHistory();
        assertNotNull(history);
    }
}