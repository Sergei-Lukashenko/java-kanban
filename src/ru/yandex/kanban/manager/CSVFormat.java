package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;

public final class CSVFormat {

    public static String getHeader() {
        return "id,type,name,status,description,epic" + System.lineSeparator();
    }

    public static String toString(Task task) {
        // Structure of CSV-line returned: "id,type,title,status,description,startTime,duration,epic"
        TaskType taskType = TaskType.TASK;
        if (task instanceof Epic) {
            taskType = TaskType.EPIC;
        } else if (task instanceof Subtask) {
            taskType = TaskType.SUBTASK;
        }
        int id = task.getId();
        String[] lineFields = new String[] {String.valueOf(id), taskType.name(),                  // id, type
                task.getTitle().replaceAll(",", ""), task.getStatus().name(),    // title, status
                task.getDescription().replaceAll(",", ""),                       // description
                task.getStartTime().toString(), String.valueOf(task.getDuration().toMinutes()),   // startTime, duration
                (taskType == TaskType.SUBTASK ? String.valueOf(((Subtask)task).getEpicId()) : "") // epic
        };
        return String.join(",", lineFields) + System.lineSeparator();
    }

    public static Task fromString(String value) {
        // Structure of value read from CSV-file and parsed into values[]:
        //      "id,type,title,status,description,startTime,duration,epic"
        // Returns null on wrong task type read from CSV-file
        String[] values = value.split(",");
        int id = Integer.parseInt(values[0]);
        TaskType taskType = TaskType.valueOf(values[1]);
        TaskStatus status = TaskStatus.valueOf(values[3]);
        LocalDateTime startTime = values[5].equals("null") ? null : LocalDateTime.parse(values[5]);
        Duration duration = Duration.ofMinutes(Long.parseLong(values[6]));
        switch (taskType) {
            case TaskType.TASK -> {
                Task task = new Task(values[2], values[4]);
                task.setId(id);
                task.setStatus(status);
                task.setStartTime(startTime);
                task.setDuration(duration);
                return task;
            }
            case TaskType.EPIC -> {
                Epic epic = new Epic(values[2], values[4]);
                epic.setId(id);
                epic.setStatus(status);
                epic.setStartTime(startTime);
                epic.setDuration(duration);
                return epic;
            }
            case TaskType.SUBTASK -> {
                Subtask subtask = new Subtask(values[2], values[4], Integer.parseInt(values[7]));
                subtask.setId(id);
                subtask.setStatus(status);
                subtask.setStartTime(startTime);
                subtask.setDuration(duration);
                return subtask;
            }
        }
        System.err.println("This line from CSV-file has incorrect task type: " + value);
        return null;
    }

}
