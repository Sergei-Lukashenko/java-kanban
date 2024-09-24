package ru.yandex.kanban.tasks;

public class Subtask extends Task {

    protected int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        epicId = subtask.epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" + super.getTaskString() + ", parent epic id=" + epicId + '}';
    }
}
