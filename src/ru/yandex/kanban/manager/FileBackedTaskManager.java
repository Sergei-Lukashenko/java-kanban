package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) { this.file = file; }

    private void save() {
        try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8);
             BufferedWriter bufWriter = new BufferedWriter(writer)) {
            bufWriter.write(CSVFormat.getHeader());
            for (Task task : super.getTasks()) {
                bufWriter.write(CSVFormat.toString(task));
            }
            for (Epic epic : super.getEpics()) {
                bufWriter.write(CSVFormat.toString(epic));
            }
            for (Subtask subtask : super.getSubtasks()) {
                bufWriter.write(CSVFormat.toString(subtask));
            }
        } catch (IOException exception) {
            //noinspection CallToPrintStackTrace
            exception.printStackTrace();
            String shortMess = exception.getMessage();
            throw new ManagerSaveException("An error occurred on writing CSV file" + file.getName()
                    + (shortMess != null ? ": " + shortMess : ""),
                    exception
            );
        }
    }

    static FileBackedTaskManager loadFromFile(File file) {
/*      Example of CSV-file to restore manager from:
*           id,type,title,status,description,epic
*           1,TASK,Task1,NEW,Description task1,
*           2,EPIC,Epic2,DONE,Description epic2,
*           3,SUBTASK,Sub Task2,DONE,Description sub task3,2
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
        String[] fileLines = fileValue.split(System.lineSeparator());
        for (int row = 1; row < fileLines.length; row++) {   // skip 1st line of file as a header
            Task task = CSVFormat.fromString(fileLines[row]);
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
