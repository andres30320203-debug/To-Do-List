package main.java.com.todolist;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String BASE_DIR = "data";
    private static final String TODO_DIR = "to-do";
    private static final String DONE_DIR = "done";

    public FileManager() {
        createBaseDirectory();
    }

    private void createBaseDirectory() {
        File baseDir = new File(BASE_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    private String getMonthYearPath(YearMonth yearMonth) {
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();
        String monthName = yearMonth.getMonth().toString().toLowerCase();
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

        return String.format("%s/%d/%02d_%s", BASE_DIR, year, month, monthName);
    }

    private String getTaskFileName(Task task) {
        //Crear un nombre de archivo seguro para la tarea
        String safeTitle = task.getTitle().replaceAll("[^a-zA-z0-9\\s]", "_");
        String dateStr = task.getCreationDate().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s.task", dateStr, safeTitle);
    }

    public void saveTask(Task task) {
        try {
            LocalDate taskDate = task.getDueDate() != null ? task.getDueDate() : task.getCreationDate();
            YearMonth yearMonth = YearMonth.from(taskDate);

            String basePath = getMonthYearPath(yearMonth);
            String subDir = task.isCompleted() ? DONE_DIR : TODO_DIR;
            String fullPath = String.format("%s/%s", basePath, subDir);

            //Crear directorio si no existen
            File directory = new File(fullPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            //Guardar la tarea
            String filePath = String.format("%s/%s", fullPath, getTaskFileName(task));
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
                oos.writeObject(task);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTaskStatus(Task task) {
        // Primero elimiar la tarea de su ubicacion actual
        deleteTask(task);
        // Luego guardarla en la nueva ubicacion segun su estado
        saveTask(task);
    }

    public void deleteTask(Task task) {
        try {
            LocalDate taskDate = task.getDueDate() != null ? task.getDueDate() : task.getCreationDate();
            YearMonth yearMonth = YearMonth.from(taskDate);

            String basePath = getMonthYearPath(yearMonth);
            String subDir = task.isCompleted() ? DONE_DIR : TODO_DIR;
            String fullPath = String.format("$s/%s", basePath, subDir);

            File directory = new File(fullPath);
            if (directory.exists()) {
                File[] files = directory.listFiles((dir, name) -> name.endsWith(".task"));
                if (files != null) {
                    for (File file : files) {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                            Task savedTask = (Task) ois.readObject();
                            if (savedTask.getTitle().equals(task.getTitle()) && savedTask.getCreationDate().equals(task.getCreationDate())) {
                                file.delete();
                                break;
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Task> loadTasksForMonth(YearMonth yearMonth, boolean completed) {
        List<Task> tasks = new ArrayList<>();

        try {
            String basePath = getMonthYearPath(yearMonth);
            String subDir = completed ? DONE_DIR : TODO_DIR;
            String fullPath = String.format("%s/%s", basePath, subDir);

            File directory = new File(fullPath);
            if (directory.exists()) {
                File[] files = directory.listFiles((dir, name) -> name.endsWith(".task"));
                if (files != null) {
                    for (File file : files) {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                            Task task = (Task) ois.readObject();
                            tasks.add(task);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                         }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public List<YearMonth> getAvailableMonths() {
        List<YearMonth> months = new ArrayList<>();
        File baseDir = new File(BASE_DIR);
        
        if (baseDir.exists()) {
            File[] yearDirs = baseDir.listFiles(File::isDirectory);
            if (yearDirs != null) {
                for (File yearDir : yearDirs) {
                    try {
                        int year = Integer.parseInt(yearDir.getName());
                        File[] monthDirs = yearDir.listFiles(File::isDirectory);
                        if (monthDirs != null) {
                            for (File monthDir : monthDirs) {
                                try {
                                    String monthDirName = monthDir.getName();
                                    int month = Integer.parseInt(monthDirName.substring(0, 2));
                                    months.add(YearMonth.of(year, month));
                                } catch (NumberFormatException e) {
                                    // Ignorar directorios que no siguen el formato esperado
                                }
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar directorios que no son años
                    }
                }
            }
        }
        
        return months;
    }
}