package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.Task;
import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private final HistoryManager history = Managers.getDefaultHistory();
    private int seqId;

    private final Comparator<Task> comparator = new Comparator<>() {
        @Override
        public int compare(Task t1, Task t2) {
            if (t1.getStartTime() != null && t2.getStartTime() != null) {
                if (t1.getStartTime().isAfter(t2.getStartTime())) {
                    return 1;
                } else if (t1.getStartTime().isBefore(t2.getStartTime())) {
                    return -1;
                } else {
                    return 0;
                }
            } else if (t1.getStartTime() != null) {
                return 1;
            } else if (t2.getStartTime() != null) {
                return -1;
            } else {
                return t1.getId() - t2.getId();
            }
        }
    };
    private final TreeSet<Task> tasksByTime = new TreeSet<>(comparator);

    InMemoryTaskManager() {   // empty package-private constructor to avoid cross-package access,
    }                         // see also Managers.getDefault()

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
    public List<Task> getHistory() {
        return history.getHistory();
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int id) {
        Epic epic = getEpicById(id);
        if (epic == null) {
            throw new NoSuchElementException("Not found Epic with id=" + id);
        }
        return (ArrayList<Subtask>) epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .collect(Collectors.toList());
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.getOrDefault(id, null);
        history.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.getOrDefault(id, null);
        history.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.getOrDefault(id, null);
        history.add(subtask);
        return subtask;
    }

    @Override
    public int addNewTask(Task task) {
        if (tasksByTime.stream().anyMatch(t -> intersected(t, task))) {
            throw new TaskTimeConflictException("Task period conflicts with existing tasks on adding, start = " +
                    task.getStartTime() + ", end = " + task.getEndTime());
        }
        final int id = ++seqId;
        task.setId(id);
        tasks.put(id, task);
        tasksByTime.add(task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        final int id = ++seqId;
        epic.setId(id);
        epics.put(id, epic);
        updateEpicState(epic);
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        if (tasksByTime.stream().anyMatch(t -> intersected(t, subtask))) {
            throw new TaskTimeConflictException("Subtask period conflicts with existing tasks on adding, start = " +
                    subtask.getStartTime() + ", end = " + subtask.getEndTime());
        }
        final int id = ++seqId;
        subtask.setId(id);
        final int epicId = subtask.getEpicId();
        Epic epic = getEpicById(epicId);
        if (epic == null) {
            throw new NoSuchElementException("Not found Epic with id=" + epicId + " specified for subtask #" + id);
        }
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        updateEpicState(epic);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (tasksByTime.stream().anyMatch(t -> intersected(t, task))) {
            throw new TaskTimeConflictException("Task period conflicts with existing tasks on update, start = " +
                    task.getStartTime() + ", end = " + task.getEndTime());
        }
        final int taskId = task.getId();
        Task existingTask = getTaskById(taskId);
        if (existingTask == null) {
            throw new NoSuchElementException("Task with id=" + taskId + " not found. Cannot update " + task);
        }
        tasks.put(taskId, task);
        if (!task.getStartTime().equals(existingTask.getStartTime())) {
            tasksByTime.remove(task);   // to place task to the correct node in the tree set
        }
        tasksByTime.add(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        final int epicId = epic.getId();
        Epic existingEpic = getEpicById(epicId);
        if (existingEpic == null) {
            throw new NoSuchElementException("Epic with id=" + epicId + " not found. Cannot update " + epic);
        }
        epics.put(epicId, epic);
        updateEpicState(epic);
        if (tasksByTime.stream().noneMatch(t -> intersected(t, epic))) {
            tasksByTime.add(epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (tasksByTime.stream().anyMatch(t -> intersected(t, subtask))) {
            throw new TaskTimeConflictException("Subtask period conflicts with existing tasks on update, start = " +
                    subtask.getStartTime() + ", end = " + subtask.getEndTime());
        }
        final int subtaskId = subtask.getId();
        Subtask existingSubtask = getSubtaskById(subtaskId);
        if (existingSubtask == null) {
            throw new NoSuchElementException("Subtask with id=" + subtaskId + " not found. Cannot update " + subtask);
        }
        final int epicId = subtask.getEpicId();
        Epic epic = getEpicById(epicId);
        if (epic == null) {
            throw new NoSuchElementException("Not found Epic with id=" + epicId + " specified for subtask #" + subtaskId);
        }
        subtasks.put(subtaskId, subtask);
        updateEpicState(epic);
        if (!subtask.getStartTime().equals(existingSubtask.getStartTime())) {
            tasksByTime.remove(subtask);   // to place subtask to the correct node in the tree set
        }
        tasksByTime.add(subtask);
    }

    @Override
    public void deleteTask(int id) {
        tasksByTime.remove(tasks.get(id));
        tasks.remove(id);
        history.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) {
            return;
        }
        Epic epic = getEpicById(id);
        epics.remove(id);
        history.remove(id);
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
        Subtask subtask = getSubtaskById(id);
        final int epicId = subtask.getEpicId();
        tasksByTime.remove(subtask);
        subtasks.remove(id);
        history.remove(id);
        Epic epic = getEpicById(epicId);
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicState(epic);
        }
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
        history.clear();
        tasksByTime.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            tasksByTime.remove(subtask);
        }
        subtasks.clear();
        for (Integer epicId : epics.keySet()) {
            updateEpicState(getEpicById(epicId));
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(tasksByTime);
    }

    private boolean intersected(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime start2 = task2.getStartTime();
        if (start1 == null || start2 == null) {
            return true;
        }
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime end2 = task2.getEndTime();
        return (start1.isBefore(start2) && end1.isAfter(start2)) ||
                (start2.isBefore(end2) && end1.isAfter(end2));
    }

    private void updateEpicState(Epic epic) {
        HashSet<TaskStatus> subtaskStatusSet = new HashSet<>();
        Duration totalDuration = Duration.ofMinutes(0);
        LocalDateTime minStart = LocalDateTime.MAX;
        LocalDateTime maxEnd = LocalDateTime.MIN;
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = getSubtaskById(subtaskId);
            if (subtask == null) {
                continue;
            }
            subtaskStatusSet.add(subtask.getStatus());
            totalDuration.plus(subtask.getDuration());
            LocalDateTime start = subtask.getStartTime();
            LocalDateTime end = subtask.getEndTime();
            if (start.isBefore(minStart)) {
                minStart = start;
            }
            if (end.isAfter(maxEnd)) {
                maxEnd = end;
            }
        }
        if (subtaskStatusSet.isEmpty() || (subtaskStatusSet.size() == 1 && subtaskStatusSet.contains(TaskStatus.NEW))) {
            // у Эпика нет подзадач или все они имеют статус NEW --> статус Эпика должен быть NEW
            epic.setStatus(TaskStatus.NEW);
        } else if (subtaskStatusSet.size() == 1 && subtaskStatusSet.contains(TaskStatus.DONE)) {
            // если все подзадачи имеют статус DONE --> Эпик считается завершённым — со статусом DONE
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
        epic.setDuration(totalDuration);
        epic.setStartTime(minStart);
        epic.setEndTime(maxEnd);
    }

}
