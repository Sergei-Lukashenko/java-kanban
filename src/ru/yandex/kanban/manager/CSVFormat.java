package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.*;

public final class CSVFormat {

    public static String getHeader() {
        return "id,type,name,status,description,epic" + System.lineSeparator();
    }

    public static String toString(Task task) {
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
        return String.join(",", lineFields) + System.lineSeparator();
    }

    public static Task fromString(String value) {
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

}
