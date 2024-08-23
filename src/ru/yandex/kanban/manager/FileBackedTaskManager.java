package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final File file;

    FileBackedTaskManager(File file) {
        this.file = file;
    }

    void save() {
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8);
             BufferedWriter bufWriter = new BufferedWriter(writer)) {
            bufWriter.write("id,type,name,status,description,epic" + '\n');
            for (Task task : super.getTasks()) {
                bufWriter.write(toString(task));
            }
            for (Epic epic : super.getEpics()) {
                bufWriter.write(toString(epic));
            }
            for (Subtask subtask : super.getSubtasks()) {
                bufWriter.write(toString(subtask));
            }
        } catch (IOException exception) {
            //noinspection CallToPrintStackTrace
            exception.printStackTrace();
            String shortMess = exception.getMessage();
            throw new ManagerSaveException("An error occurred on writing CSV file"
                    + (shortMess != null ? ": " + shortMess : "")
            );
        }
    }

    private String toString(Task task) {
        // Structure of CSV-line returned: "id,type,title,status,description,epic"
        TaskType taskType = TaskType.TASK;
        if (task instanceof Epic) {
            taskType = TaskType.EPIC;
        } else if (task instanceof Subtask) {
            taskType = TaskType.SUBTASK;
        }
        int id = task.getId();
        String[] lineFields = new String[] {String.valueOf(id), taskType.name(),                   // id, type
                task.getTitle().replaceAll(",", ""), task.getStatus().name(),    // title, status
                task.getDescription().replaceAll(",", ""),                       // description
                (taskType == TaskType.SUBTASK ? String.valueOf(((Subtask)task).getEpicId()) : "")  // epic
        };
        return String.join(",", lineFields) + '\n';
    }

    private Task fromString(String value) {
        // Structure of value read from CSV-file and parsed into values[]: "id,type,title,status,description,epic"
        // Returns null on wrong task type read from CSV-file
        String[] values = value.split(",");
        int id = Integer.parseInt(values[0]);
        TaskType taskType = TaskType.valueOf(values[1]);
        TaskStatus status = TaskStatus.valueOf(values[3]);
        switch (taskType) {
            case TaskType.TASK -> {
                Task task = new Task(values[2], values[4]);
                task.setId(id);
                task.setStatus(status);
                return task;
            }
            case TaskType.EPIC -> {
                Epic epic = new Epic(values[2], values[4]);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            }
            case TaskType.SUBTASK -> {
                Subtask subtask = new Subtask(values[2], values[4], Integer.parseInt(values[5]));
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            }
        }
        System.err.println("This line from CSV-file has incorrect task type: " + value);
        return null;
    }

    static FileBackedTaskManager loadFromFile(File file) {
/*      Example of CSV-file to restore manager from:
        id,type,title,status,description,epic
        1,TASK,Task1,NEW,Description task1,
        2,EPIC,Epic2,DONE,Description epic2,
        3,SUBTASK,Sub Task2,DONE,Description sub task3,2
*/
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        String fileValue;
        try {
            fileValue = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            //noinspection CallToPrintStackTrace
            exception.printStackTrace();
            return manager;
        }
        String[] fileLines = fileValue.split("\n");
        for (int row = 1; row < fileLines.length; row++) {   // skip 1st line of file as a header
            Task task = manager.fromString(fileLines[row]);
            if (task instanceof Epic) {
                manager.addNewEpic((Epic)task);
            } else if (task instanceof Subtask) {
                manager.addNewSubtask((Subtask)task);
            } else {
                manager.addNewTask(task);
            }
        }
        return manager;
    }

    @Override
    public int addNewTask(Task task) {
        int newTaskId = super.addNewTask(task);
        save();
        return newTaskId;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int newEpicId = super.addNewEpic(epic);
        save();
        return newEpicId;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int newSubtaskId = super.addNewSubtask(subtask);
        save();
        return newSubtaskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
    }

}
