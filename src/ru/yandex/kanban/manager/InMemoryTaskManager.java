package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.Task;
import ru.yandex.kanban.tasks.Epic;
import ru.yandex.kanban.tasks.Subtask;
import ru.yandex.kanban.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.naturalOrder;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private final HistoryManager history = Managers.getDefaultHistory();
    private int seqId;

    static private final Comparator<Task> BY_TIME = comparing(Task::getStartTime,
            nullsFirst(naturalOrder()))
            .thenComparing(Task::getId);
    private final TreeSet<Task> tasksByTime = new TreeSet<>(BY_TIME);

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
    public List<Subtask> getEpicSubtasks(int id) {
        Epic epic = getEpicById(id);
        if (epic == null) {
            throw new NoSuchElementException("Not found Epic with ID=" + id);
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .toList();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.getOrDefault(id, null);
        if (task != null) {
            history.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.getOrDefault(id, null);
        if (epic != null) {
            history.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.getOrDefault(id, null);
        if (subtask != null) {
            history.add(subtask);
        }
        return subtask;
    }

    @Override
    public int addNewTask(Task task) {
        if (tasksByTime.stream().anyMatch(t -> overlapped(t, task))) {
            throw new TaskOverlapException(
                    String.format("Task '%s' period conflicts with existing tasks on adding, start=%s, end=%s",
                            task.getTitle(), task.getStartTime().toString(), task.getEndTime().toString())
            );
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
        if (tasksByTime.stream().anyMatch(t -> overlapped(t, subtask))) {
            throw new TaskOverlapException(
                    String.format("Subtask '%s' period conflicts with existing tasks on adding, start=%s, end=%s",
                            subtask.getTitle(), subtask.getStartTime().toString(), subtask.getEndTime().toString())
            );
        }
        final int id = ++seqId;
        subtask.setId(id);
        final int epicId = subtask.getEpicId();
        Epic epic = getEpicById(epicId);
        if (epic == null) {
            throw new NoSuchElementException("Not found Epic with ID=" + epicId + " specified for subtask #" + id);
        }
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        tasksByTime.add(subtask);
        updateEpicState(epic);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        final int taskId = task.getId();
        if (tasksByTime.stream().anyMatch(t -> overlapped(t, task))) {
            throw new TaskOverlapException(
                    String.format("Task #%d period conflicts with existing tasks on update, start=%s, end=%s",
                            taskId, task.getStartTime().toString(), task.getEndTime().toString())
            );
        }
        Task existingTask = getTaskById(taskId);
        if (existingTask == null) {
            throw new NoSuchElementException("Task with ID=" + taskId + " not found. Cannot update " + task);
        }
        if (!task.equals(existingTask)) {
            tasks.put(taskId, task);
            // remove-add to place task to the correct node in the tree set
            tasksByTime.remove(task);
            tasksByTime.add(task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        final int epicId = epic.getId();
        Epic existingEpic = getEpicById(epicId);
        if (existingEpic == null) {
            throw new NoSuchElementException("Epic with ID=" + epicId + " not found. Cannot update " + epic);
        }
        epics.put(epicId, epic);
        updateEpicState(epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        final int subtaskId = subtask.getId();
        if (tasksByTime.stream().anyMatch(t -> overlapped(t, subtask))) {
            throw new TaskOverlapException(
                    String.format("Subtask #%d period conflicts with existing tasks on update, start=%s, end=%s",
                            subtaskId, subtask.getStartTime().toString(), subtask.getEndTime().toString())
            );
        }
        Subtask existingSubtask = getSubtaskById(subtaskId);
        if (existingSubtask == null) {
            throw new NoSuchElementException("Subtask with ID=" + subtaskId + " not found. Cannot update " + subtask);
        }
        final int epicId = subtask.getEpicId();
        Epic epic = getEpicById(epicId);
        if (epic == null) {
            throw new NoSuchElementException("Not found Epic with ID=" + epicId + " specified for subtask #" + subtaskId);
        }
        if (!subtask.equals(existingSubtask)) {
            subtasks.put(subtaskId, subtask);
            // remove-add to place subtask to the correct node in the tree set
            tasksByTime.remove(subtask);
            tasksByTime.add(subtask);
            updateEpicState(epic);
        }
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
        deleteAllSubtasks();
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

    private boolean overlapped(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime start2 = task2.getStartTime();
        if (start1 == null || start2 == null) {
            return true;
        }
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime end2 = task2.getEndTime();
        return (start1.isBefore(start2) && end1.isAfter(start2)) ||
                (start1.isBefore(end2) && end1.isAfter(end2));
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
        if (!subtaskStatusSet.isEmpty()) {  // some subtasks present
            epic.setStartTime(minStart);
            epic.setEndTime(maxEnd);
        }
    }

}
