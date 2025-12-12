package main.java.com.todolist;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean completed;
    private LocalDate completionDate;
    private LocalDate creationDate;
    private String category;

    public Task(String title, String description, LocalDate dueDate, String category) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.category = category;
        this.completed = false;
        this.creationDate = LocalDate.now();
        this.completionDate = null;
    }

    //Getters y Setters
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

    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public void markAsCompleted() {
        this.completed = true;
        this.completionDate = LocalDate.now();
    }

    public void markAsPending() {
        this.completed = false;
        this.completionDate = null;
    }

    public String getFormattedDueDate() {
        if (dueDate == null) return "Sin fecha";
        return dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedCreationDate() {
        return creationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Override
    public String toString() {
        String status = completed ? "[✓]" : "[ ]";
        return String.format("%s %s (Vence: %s, Categoria: %s)", status, title, getFormattedDueDate(), category);
    }
}
