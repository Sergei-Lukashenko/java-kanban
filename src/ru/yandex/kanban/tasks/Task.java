package ru.yandex.kanban.tasks;

import java.util.Objects;

public class Task {

    protected int id;
    protected String title;
    protected String description;

    protected TaskStatus status;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.NEW;
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
        return id == task.id && Objects.equals(title, task.title) && Objects.equals(description, task.description) && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status);
    }

    @Override
    public String toString() { return "Task{" + getTaskString() + '}';  }

    String getTaskString() {
        String result = "id=" + id +
                        ", title='" + title + '\'' +
                        ", status=" + status;
        if (description == null) {
            result += ", description is empty";
        } else {
            result += ", description length=" + description.length();
        }
        return result;
    }
}
