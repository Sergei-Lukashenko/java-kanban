package ru.yandex.kanban.manager;

import ru.yandex.kanban.tasks.Task;
import ru.yandex.kanban.tasks.Node;

import java.util.*;

class InMemoryHistoryManager implements HistoryManager {

    // Task ID  --> "Noded" Task in doubly-linked list
    private final Map<Integer, Node> nodeStorage = new HashMap<>();

    private final CustomLinkedList history = new CustomLinkedList();

    InMemoryHistoryManager() {   // empty package-private constructor to avoid cross-package access,
    }                            // see also Managers.getDefaultHistory()

    @Override
    public void add(Task task) {
        if (task == null) {
            throw new RuntimeException("Task is null. InMemoryHistoryManager.add()");
        }
        final int id = task.getId();
        Node node = nodeStorage.get(id);
        if (node != null) {
            history.removeNode(node);
        }
        nodeStorage.put(id, history.linkLast(task));
    }

    @Override
    public void remove(int id) {
        Node node = nodeStorage.get(id);
        if (node == null) {
            throw new RuntimeException("Node for removing not found for Task id=" + id);
        }
        history.removeNode(node);
        nodeStorage.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    @Override
    public void clear() {
        history.clear();
        nodeStorage.clear();
    }

    private static class CustomLinkedList {
        private Node head;
        private Node tail;
        private int size = 0;

        public Node linkLast(Task task) {
            final Node oldTail = tail;
            final Node newNode = new Node(oldTail, task, null);
            tail = newNode;
            if (oldTail == null) {
                head = newNode;
            } else {
                oldTail.next = newNode;
            }
            size++;
            return newNode;
        }

        ArrayList<Task> getTasks() {
            ArrayList<Task> history = new ArrayList<>();
            Node item = head;
            if (item == null) {
                return history;
            }
            history.add(item.task);
            while (item.next != null) {
                item = item.next;
                history.add(item.task);
            }
            return history;
        }

        void removeNode(Node node) {
            final Node next = node.next;
            final Node prev = node.prev;

            if (prev == null) {
                head = next;
            } else {
                prev.next = next;
                node.prev = null;
            }

            if (next == null) {
                tail = prev;
            } else {
                next.prev = prev;
                node.next = null;
            }

            node.task = null;
            size--;
        }

        void clear() {
            head = tail = null;
            size = 0;
        }
    }
}