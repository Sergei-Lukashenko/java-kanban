package ru.yandex.kanban.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task implements Comparable<Task> {

    protected int id;
    protected String title;
    protected String description;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        status = TaskStatus.NEW;
        duration = Duration.ofMinutes(0);
        startTime = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public boolean isEpic() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(title, task.title) && Objects.equals(description, task.description)
                && status == task.status && Objects.equals(startTime, task.startTime) && Objects.equals(duration, task.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status, startTime, duration);
    }

    @Override
    public String toString() {
        return "Task{" + getTaskString() + '}';
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime == null ? null : startTime.plus(duration);
    }

    @Override
    public int compareTo(Task other) {
        if (getStartTime() != null && other.getStartTime() != null) {
            if (getStartTime().isAfter(other.getStartTime())) {
                return 1;
            } else if (getStartTime().isBefore(other.getStartTime())) {
                return -1;
            } else {
                return 0;
            }
        } else if (getStartTime() != null) {
            return 1;
        } else if (other.getStartTime() != null) {
            return -1;
        } else {
            return Integer.compare(getId(), other.getId());
        }
    }

    String getTaskString() {
        String result = "id=" + id +
                        ", title='" + title + '\'' +
                        ", status=" + status;
        if (description == null) {
            result += ", description is empty";
        } else {
            result += ", description length=" + description.length();
        }
        if (startTime != null) {
            result += ", startTime=" + startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss:SSS"));
        }
        if (duration != null) {
            result += ", duration=" + duration;
        }
        return result;
    }
}
