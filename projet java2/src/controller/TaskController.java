
package controller;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskController {
    private List<Task> tasks;

    public TaskController() {
        tasks = new ArrayList<>();
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public void updateTask(Task oldTask, Task newTask) {
        int index = tasks.indexOf(oldTask);
        if (index != -1) {
            tasks.set(index, newTask);
        }
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public List<Task> searchTasksByTitle(String title) {
        List<Task> foundTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getTitle().equalsIgnoreCase(title)) {
                foundTasks.add(task);
            }
        }
        return foundTasks;
    }
}
