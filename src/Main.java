import ru.yandex.kanban.manager.TaskManager;
import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.Task;
import ru.yandex.kanban.tasks.TaskStatus;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = new TaskManager();

        // Создание
        Task task1 = new Task("Task #1", "Task1 description"); // TaskStatus.NEW
        Task task2 = new Task("Task #2", "Task2 description");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        final int taskId1 = manager.addNewTask(task1);
        final int taskId2 = manager.addNewTask(task2);

        Task storedTask1 = manager.getTask(taskId1);
        System.out.println("storedTask1.id = " + storedTask1.getId() + ", task1.id = " + task1.getId());

        ArrayList<Task> tasks = manager.getTasks();
        if (tasks.contains(task1) && tasks.contains(task2)) {
            System.out.println("Task1 и Task2 добавлены успешно");
        } else {
            System.out.println("Какие-то проблемы с добавлением Task1 и Task2");
        }

        Epic epic1 = new Epic("Epic #1", "Epic1 description");   // TaskStatus.NEW
        Epic epic2 = new Epic("Epic #2", "Epic2 description");   // TaskStatus.NEW
        final int epicId1 = manager.addNewEpic(epic1);
        final int epicId2 = manager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", epicId1);  // TaskStatus.NEW
        Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask2 description", epicId1);  // TaskStatus.NEW
        Subtask subtask3 = new Subtask("Subtask #3-2", "Subtask3 description", epicId2);
        subtask3.setStatus(TaskStatus.DONE);

        final int subtaskId1 = manager.addNewSubtask(subtask1);
        final int subtaskId2 = manager.addNewSubtask(subtask2);
        final int subtaskId3 = manager.addNewSubtask(subtask3);

        Epic checkedEpic = manager.getEpic(epicId1);
        if (checkedEpic.getStatus() == TaskStatus.NEW) {
            System.out.println("epic1.status == NEW, как и должно быть");
        } else {
            System.out.println("Что-то не так со статусом epic1");
        }
        checkedEpic = manager.getEpic(epicId2);
        if (checkedEpic.getStatus() == TaskStatus.DONE) {
            System.out.println("epic2.status == DONE, как и должно быть");
        } else {
            System.out.println("Что-то не так со статусом epic2");
        }

        printAllTasks(manager);

        // Обновление
        final Task task = manager.getTask(taskId2);
        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);
        System.out.println("updateTask() для task2 с IN_PROGRESS в DONE выполнен");
        printTasks(manager);

        final Subtask subtask = manager.getSubtask(subtaskId3);
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask);
        System.out.println("updateSubtask() для subtask3 с DONE в IN_PROGRESS выполнен");
        printSubtasks(manager);
        System.out.println("При этом статус Эпик 2 стал " + epic2);

        // Удаление
        manager.deleteTask(taskId2);
        manager.deleteSubtask(subtaskId3);
        System.out.println("Удалили task2 и subtask3");
        printAllTasks(manager);

    }

    static void printAllTasks(TaskManager manager) {
        System.out.println("Tasks:");
        for (Task t : manager.getTasks()) {
            System.out.println("\t" + t);
        }
        System.out.println("Epics:");
        for (Epic e : manager.getEpics()) {
            System.out.println("\t" + e);
        }
        System.out.println("Subtasks:");
        for (Subtask s : manager.getSubtasks()) {
            System.out.println("\t" + s);
        }
    }

    static void printTasks(TaskManager manager) {
        System.out.println("Tasks:");
        for (Task t : manager.getTasks()) {
            System.out.println("\t" + t);
        }
    }

    static void printSubtasks(TaskManager manager) {
        System.out.println("Subtasks:");
        for (Subtask s : manager.getSubtasks()) {
            System.out.println("\t" + s);
        }
    }
}
