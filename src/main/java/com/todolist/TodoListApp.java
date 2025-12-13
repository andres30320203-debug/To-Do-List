package main.java.com.todolist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TodoListApp extends JFrame {
    private TaskManager taskManager;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel monthLabel;
    private JComboBox<String> categoryFilter;
    
    public TodoListApp() {
        taskManager = new TaskManager();
        initializeUI();
        refreshTaskTable();
    }
    
    private void initializeUI() {
        setTitle("To-Do List App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior con controles
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel de navegación de meses
        JPanel monthPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        monthLabel = new JLabel();
        updateMonthLabel();
        
        JButton prevMonthBtn = new JButton("<");
        prevMonthBtn.addActionListener(e -> {
            taskManager.navigateToPreviousMonth();
            updateMonthLabel();
            refreshTaskTable();
        });
        
        JButton nextMonthBtn = new JButton(">");
        nextMonthBtn.addActionListener(e -> {
            taskManager.navigateToNextMonth();
            updateMonthLabel();
            refreshTaskTable();
        });
        
        JButton currentMonthBtn = new JButton("Hoy");
        currentMonthBtn.addActionListener(e -> {
            taskManager.navigateToCurrentMonth();
            updateMonthLabel();
            refreshTaskTable();
        });
        
        monthPanel.add(prevMonthBtn);
        monthPanel.add(monthLabel);
        monthPanel.add(nextMonthBtn);
        monthPanel.add(currentMonthBtn);
        
        // Panel de filtros
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filtrar por:"));
        
        String[] categories = {"Todas", "Trabajo", "Personal", "Estudio", "Salud", "Otros"};
        categoryFilter = new JComboBox<>(categories);
        categoryFilter.addActionListener(e -> refreshTaskTable());
        
        JButton showPendingBtn = new JButton("Pendientes");
        showPendingBtn.addActionListener(e -> showPendingTasks());
        
        JButton showCompletedBtn = new JButton("Completadas");
        showCompletedBtn.addActionListener(e -> showCompletedTasks());
        
        filterPanel.add(categoryFilter);
        filterPanel.add(showPendingBtn);
        filterPanel.add(showCompletedBtn);
        
        topPanel.add(monthPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);
        
        // Panel central con la tabla de tareas
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new TitledBorder("Tareas"));
        
        // Modelo de tabla
        String[] columnNames = {"Estado", "Título", "Descripción", "Fecha Vencimiento", "Categoría", "Creada"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }
        };
        
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(taskTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior con botones
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton addBtn = new JButton("Agregar Tarea");
        addBtn.addActionListener(e -> showAddTaskDialog());
        
        JButton editBtn = new JButton("Editar Tarea");
        editBtn.addActionListener(e -> editSelectedTask());
        
        JButton completeBtn = new JButton("Marcar como Completada");
        completeBtn.addActionListener(e -> toggleTaskCompletion());
        
        JButton deleteBtn = new JButton("Eliminar Tarea");
        deleteBtn.addActionListener(e -> deleteSelectedTask());
        
        JButton viewFilesBtn = new JButton("Ver Archivos");
        viewFilesBtn.addActionListener(e -> openDataFolder());
        
        bottomPanel.add(addBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(completeBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(viewFilesBtn);
        
        // Agregar paneles al frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Configurar ventana
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        // Agregar listener para doble clic en la tabla
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedTask();
                }
            }
        });
    }
    
    private void updateMonthLabel() {
        YearMonth currentMonth = taskManager.getCurrentViewMonth();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        String monthStr = currentMonth.format(formatter);
        monthStr = monthStr.substring(0, 1).toUpperCase() + monthStr.substring(1);
        monthLabel.setText(monthStr);
    }
    
    private void refreshTaskTable() {
        tableModel.setRowCount(0);
        
        List<Task> tasks = taskManager.getTasks();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        
        for (Task task : tasks) {
            // Filtrar por categoría si no es "Todas"
            if (!"Todas".equals(selectedCategory) && !selectedCategory.equals(task.getCategory())) {
                continue;
            }
            
            String status = task.isCompleted() ? "✓" : " ";
            String dueDate = task.getFormattedDueDate();
            String createdDate = task.getFormattedCreationDate();
            
            tableModel.addRow(new Object[]{
                status,
                task.getTitle(),
                task.getDescription(),
                dueDate,
                task.getCategory(),
                createdDate
            });
        }
    }
    
    private void showPendingTasks() {
        tableModel.setRowCount(0);
        
        List<Task> tasks = taskManager.getPendingTasks();
        for (Task task : tasks) {
            String dueDate = task.getFormattedDueDate();
            String createdDate = task.getFormattedCreationDate();
            
            tableModel.addRow(new Object[]{
                " ",
                task.getTitle(),
                task.getDescription(),
                dueDate,
                task.getCategory(),
                createdDate
            });
        }
    }
    
    private void showCompletedTasks() {
        tableModel.setRowCount(0);
        
        List<Task> tasks = taskManager.getCompletedTasks();
        for (Task task : tasks) {
            String dueDate = task.getFormattedDueDate();
            String createdDate = task.getFormattedCreationDate();
            
            tableModel.addRow(new Object[]{
                "✓",
                task.getTitle(),
                task.getDescription(),
                dueDate,
                task.getCategory(),
                createdDate
            });
        }
    }
    
    private void showAddTaskDialog() {
        JDialog dialog = new JDialog(this, "Agregar Nueva Tarea", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Campos del formulario
        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descArea);
        JTextField dueDateField = new JTextField(10);
        dueDateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Trabajo", "Personal", "Estudio", "Salud", "Otros"});
        
        // Configurar GridBag
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Título:"), gbc);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        formPanel.add(descScroll, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Fecha Vencimiento (dd/MM/yyyy):"), gbc);
        gbc.gridx = 1;
        formPanel.add(dueDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        formPanel.add(categoryCombo, gbc);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Guardar");
        JButton cancelBtn = new JButton("Cancelar");
        
        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();
            String dueDateStr = dueDateField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El título es requerido", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDate dueDate = null;
            try {
                if (!dueDateStr.isEmpty()) {
                    dueDate = LocalDate.parse(dueDateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Formato de fecha inválido. Use dd/MM/yyyy", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Task newTask = new Task(title, description, dueDate, category);
            taskManager.addTask(newTask);
            refreshTaskTable();
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void editSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una tarea para editar", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Obtener la tarea seleccionada
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        Task taskToEdit = null;
        
        for (Task task : taskManager.getTasks()) {
            if (task.getTitle().equals(title)) {
                taskToEdit = task;
                break;
            }
        }
        
        if (taskToEdit == null) return;

        final Task taskToEditRef = taskToEdit;
        
        JDialog dialog = new JDialog(this, "Editar Tarea", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Campos del formulario con valores actuales
        JTextField titleField = new JTextField(taskToEdit.getTitle(), 20);
        JTextArea descArea = new JTextArea(taskToEdit.getDescription(), 3, 20);
        JScrollPane descScroll = new JScrollPane(descArea);
        
        String dueDateStr = taskToEdit.getDueDate() != null ? 
            taskToEdit.getFormattedDueDate() : "";
        JTextField dueDateField = new JTextField(dueDateStr, 10);
        
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Trabajo", "Personal", "Estudio", "Salud", "Otros"});
        categoryCombo.setSelectedItem(taskToEdit.getCategory());
        
        // Configurar GridBag
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Título:"), gbc);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        formPanel.add(descScroll, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Fecha Vencimiento (dd/MM/yyyy):"), gbc);
        gbc.gridx = 1;
        formPanel.add(dueDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        formPanel.add(categoryCombo, gbc);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Guardar");
        JButton cancelBtn = new JButton("Cancelar");
        
        saveBtn.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newDescription = descArea.getText().trim();
            String newDueDateStr = dueDateField.getText().trim();
            String newCategory = (String) categoryCombo.getSelectedItem();
            
            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El título es requerido", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDate newDueDate = null;
            try {
                if (!newDueDateStr.isEmpty()) {
                    newDueDate = LocalDate.parse(newDueDateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Formato de fecha inválido. Use dd/MM/yyyy", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Task updatedTask = new Task(newTitle, newDescription, newDueDate, newCategory);
            if (taskToEditRef.isCompleted()) {
                updatedTask.markAsCompleted();
            }
            
            taskManager.updateTask(taskToEditRef, updatedTask);
            refreshTaskTable();
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void toggleTaskCompletion() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una tarea", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String title = (String) tableModel.getValueAt(selectedRow, 1);
        Task taskToToggle = null;
        
        for (Task task : taskManager.getTasks()) {
            if (task.getTitle().equals(title)) {
                taskToToggle = task;
                break;
            }
        }
        
        if (taskToToggle != null) {
            if (taskToToggle.isCompleted()) {
                taskManager.markTaskAsPending(taskToToggle);
            } else {
                taskManager.markTaskAsCompleted(taskToToggle);
            }
            refreshTaskTable();
        }
    }
    
    private void deleteSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una tarea para eliminar", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de eliminar esta tarea?", "Confirmar eliminación", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String title = (String) tableModel.getValueAt(selectedRow, 1);
            Task taskToDelete = null;
            
            for (Task task : taskManager.getTasks()) {
                if (task.getTitle().equals(title)) {
                    taskToDelete = task;
                    break;
                }
            }
            
            if (taskToDelete != null) {
                taskManager.deleteTask(taskToDelete);
                refreshTaskTable();
            }
        }
    }
    
    private void openDataFolder() {
        try {
            File dataFolder = new File("data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            Desktop desktop = Desktop.getDesktop();
            desktop.open(dataFolder);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la carpeta de datos: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}