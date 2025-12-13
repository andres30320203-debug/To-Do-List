package main.java.com.todolist;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private List<Task> tasks;
    private FileManager fileManager;
    private YearMonth currentViewMonth;
    
    public TaskManager() {
        this.tasks = new ArrayList<>();
        this.fileManager = new FileManager();
        this.currentViewMonth = YearMonth.now();
        loadTasksForCurrentMonth();
    }
    
    public void addTask(Task task) {
        tasks.add(task);
        fileManager.saveTask(task);
    }
    
    public void markTaskAsCompleted(Task task) {
        task.markAsCompleted();
        fileManager.updateTaskStatus(task);
    }
    
    public void markTaskAsPending(Task task) {
        task.markAsPending();
        fileManager.updateTaskStatus(task);
    }
    
    public void deleteTask(Task task) {
        tasks.remove(task);
        fileManager.deleteTask(task);
    }
    
    public void updateTask(Task oldTask, Task newTask) {
        // Eliminar la tarea vieja
        deleteTask(oldTask);
        // Agregar la tarea nueva
        addTask(newTask);
    }
    
    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }
    
    public List<Task> getPendingTasks() {
        List<Task> pendingTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                pendingTasks.add(task);
            }
        }
        return pendingTasks;
    }
    
    public List<Task> getCompletedTasks() {
        List<Task> completedTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isCompleted()) {
                completedTasks.add(task);
            }
        }
        return completedTasks;
    }
    
    public void setCurrentViewMonth(YearMonth yearMonth) {
        this.currentViewMonth = yearMonth;
        loadTasksForCurrentMonth();
    }
    
    public YearMonth getCurrentViewMonth() {
        return currentViewMonth;
    }
    
    private void loadTasksForCurrentMonth() {
        tasks.clear();
        // Cargar tareas pendientes
        tasks.addAll(fileManager.loadTasksForMonth(currentViewMonth, false));
        // Cargar tareas completadas
        tasks.addAll(fileManager.loadTasksForMonth(currentViewMonth, true));
    }
    
    public List<YearMonth> getAvailableMonths() {
        return fileManager.getAvailableMonths();
    }
    
    public void navigateToPreviousMonth() {
        currentViewMonth = currentViewMonth.minusMonths(1);
        loadTasksForCurrentMonth();
    }
    
    public void navigateToNextMonth() {
        currentViewMonth = currentViewMonth.plusMonths(1);
        loadTasksForCurrentMonth();
    }
    
    public void navigateToCurrentMonth() {
        currentViewMonth = YearMonth.now();
        loadTasksForCurrentMonth();
    }
}