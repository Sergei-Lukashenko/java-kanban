package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.Task;
import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    HistoryManager history = Managers.getDefaultHistory();
    
    private int seqId;

    InMemoryTaskManager() {}  // empty package-private constructor to avoid cross-package access,
                              // see also Managers.getDefault()

    @Override
    public ArrayList<Task> getTasks() { return new ArrayList<>(tasks.values()); }

    @Override
    public ArrayList<Epic> getEpics() { return new ArrayList<>(epics.values()); }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Task> getHistory() { return history.getHistory(); }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int id) {
        Epic epic = getEpic(id);
        if (epic == null) {
            throw new RuntimeException("Not found Epic with id=" + id);
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
        Task task = tasks.getOrDefault(id, null);
        history.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.getOrDefault(id, null);
        history.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.getOrDefault(id, null);
        history.add(subtask);
        return subtask;
    }

    @Override
    public int addNewTask(Task task) {
        final int id = ++seqId;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        final int id = ++seqId;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        final int id = ++seqId;
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
    public void deleteTask(int id) { tasks.remove(id); }

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

    private void updateEpicStatus(Epic epic) {
        HashSet<TaskStatus> subtaskStatusSet = new HashSet<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = getSubtask(subtaskId);
            if (subtask != null) {
                subtaskStatusSet.add(subtask.getStatus());
            }
        }
        if (subtaskStatusSet.isEmpty() || (subtaskStatusSet.size()==1 && subtaskStatusSet.contains(TaskStatus.NEW))) {
            // у Эпика нет подзадач или все они имеют статус NEW --> статус Эпика должен быть NEW
            epic.setStatus(TaskStatus.NEW);
        } else if (subtaskStatusSet.size()==1 && subtaskStatusSet.contains(TaskStatus.DONE)) {
            // если все подзадачи имеют статус DONE --> Эпик считается завершённым — со статусом DONE
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

}
