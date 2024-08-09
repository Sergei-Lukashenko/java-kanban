package ru.yandex.kanban.tasks;

import java.util.ArrayList;

public class Epic extends Task {

    protected ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    @Override
    public boolean isEpic() {
        return true;
    }

    public void addSubtaskId(int id) {
        if (!subtaskIds.contains(id)) {
            subtaskIds.add(id);
        }
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void cleanSubtaskIds() {
        subtaskIds.clear();
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove(Integer.valueOf(id));  // remove Integer object, not by int index!
    }

    @Override
    public String toString() {
        return "Epic{" + super.getTaskString() + " with " + subtaskIds.size() + " subtasks}";
    }
}
