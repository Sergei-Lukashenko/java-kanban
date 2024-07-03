package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.Task;
import ru.yandex.kanban.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TaskManager implements ITaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private int sequencedId;

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = getEpic(epicId);
        if (epic == null) {
            throw new RuntimeException("Not found Epic with id=" + epicId);
        }
        ArrayList<Subtask> subtaskList = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            subtaskList.add(subtask);
        }
        return subtaskList;
    }

    @Override
    public Task getTask(int id) {
        return tasks.getOrDefault(id, null);
    }

    @Override
    public Epic getEpic(int id) {
        return epics.getOrDefault(id, null);
    }

    @Override
    public Subtask getSubtask(int id) {
        return subtasks.getOrDefault(id, null);
    }

    @Override
    public int addNewTask(Task task) {
        final int id = ++sequencedId;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        final int id = ++sequencedId;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        final int id = ++sequencedId;
        subtask.setId(id);
        final int epicId = subtask.getEpicId();
        Epic epic = getEpic(epicId);
        if (epic == null) {
            throw new RuntimeException("Not found Epic with id=" + epicId + " specified for subtask #" + id);
        }
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        updateEpicStatus(epic);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        final int taskId = task.getId();
        Task existingTask = getTask(taskId);
        if (existingTask == null) {
            throw new RuntimeException("Task with id=" + taskId + " not found. Cannot update " + task);
        }
        tasks.put(taskId, task);
    }

    @Override
    public void updateEpic(Epic epic) {
        final int epicId = epic.getId();
        Epic existingEpic = getEpic(epicId);
        if (existingEpic == null) {
            throw new RuntimeException("Epic with id=" + epicId + " not found. Cannot update " + epic);
        }
        epics.put(epicId, epic);
        updateEpicStatus(epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        final int subtaskId = subtask.getId();
        Subtask existingSubtask = getSubtask(subtaskId);
        if (existingSubtask == null) {
            throw new RuntimeException("Subtask with id=" + subtaskId + " not found. Cannot update " + subtask);
        }
        final int epicId = subtask.getEpicId();
        Epic epic = getEpic(epicId);
        if (epic == null) {
            throw new RuntimeException("Not found Epic with id=" + epicId + " specified for subtask #" + subtaskId);
        }
        subtasks.put(subtaskId, subtask);
        updateEpicStatus(epic);
    }

    @Override
    public void deleteTask(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) {
            return;
        }
        Epic epic = getEpic(id);
        epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                deleteSubtask(subtaskId);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            return;
        }
        Subtask subtask = getSubtask(id);
        final int epicId = subtask.getEpicId();
        Epic epic = getEpic(epicId);
        subtasks.remove(id);
        epic.removeSubtaskId(id);
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Integer epicId : epics.keySet()) {
            updateEpicStatus(getEpic(epicId));
        }
    }

    void updateEpicStatus(Epic epic) {
        HashSet<TaskStatus> subtaskStatusSet = new HashSet<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = getSubtask(subtaskId);
            if (subtask != null) {
                subtaskStatusSet.add(subtask.getStatus());
            }
        }
        if (subtaskStatusSet.isEmpty() || (subtaskStatusSet.size()==1 && subtaskStatusSet.contains(TaskStatus.NEW))) {
            // у Эпика нет подзадач или все они имеют статус NEW, то и статус Эпика должен быть NEW
            epic.setStatus(TaskStatus.NEW);
        } else if (subtaskStatusSet.size()==1 && subtaskStatusSet.contains(TaskStatus.DONE)) {
            // если все подзадачи имеют статус DONE, то и Эпик считается завершённым — со статусом DONE
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

}
