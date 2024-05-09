package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskManagerFrame extends JFrame {

    private JTextField titleField;
    private JTextArea descriptionArea;
    private JTextField deadlineField;
    private JComboBox<String> priorityComboBox;
    private List<Task> tasks; // Liste des tâches

    public TaskManagerFrame() {
        super("Gestion de tâches");
        tasks = new ArrayList<>(); // Initialisation de la liste des tâches

        createMainPanel();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400); // Taille moyenne
        setLocationRelativeTo(null); // Centrer la fenêtre
        setVisible(true);
    }

    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(5, 2));

        JLabel titleLabel = new JLabel("Titre:");
        mainPanel.add(titleLabel);
        titleField = new JTextField();
        mainPanel.add(titleField);

        JLabel descriptionLabel = new JLabel("Description:");
        mainPanel.add(descriptionLabel);
        descriptionArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        mainPanel.add(scrollPane);

        JLabel deadlineLabel = new JLabel("Date d'échéance (dd/MM/yyyy):");
        mainPanel.add(deadlineLabel);
        deadlineField = new JTextField();
        mainPanel.add(deadlineField);

        JLabel priorityLabel = new JLabel("Priorité:");
        mainPanel.add(priorityLabel);
        String[] priorities = {"Haute", "Moyenne", "Basse"};
        priorityComboBox = new JComboBox<>(priorities);
        mainPanel.add(priorityComboBox);

        JButton addButton = new JButton("Ajouter");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });
        mainPanel.add(addButton);

        JButton showTasksButton = new JButton("Afficher les tâches");
        showTasksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTasks();
            }
        });
        mainPanel.add(showTasksButton);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    private void addTask() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String deadlineStr = deadlineField.getText();
        String priority = (String) priorityComboBox.getSelectedItem();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date deadline = null;
        try {
            deadline = dateFormat.parse(deadlineStr);
            if (deadline.before(new Date())) {
                JOptionPane.showMessageDialog(this, "La date d'échéance doit être future.");
                return;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Format de date incorrect (dd/MM/yyyy).");
            return;
        }

        Task newTask = new Task(title, description, deadline, priority);
        tasks.add(newTask);

        // Sauvegarde automatique des tâches après l'ajout
        saveTasksToFile("tasklistframe.csv");

        // Réinitialiser les champs après l'ajout
        titleField.setText("");
        descriptionArea.setText("");
        deadlineField.setText("");
        priorityComboBox.setSelectedIndex(0);

        JOptionPane.showMessageDialog(this, "Tâche ajoutée avec succès !");
    }

    private void showTasks() {
        if (tasks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune tâche disponible.");
            return;
        }

        // Sauvegarder les tâches dans un fichier CSV
        saveTasksToFile("tasklistframe.csv");

        // Afficher les tâches dans un nouveau cadre
        new TaskListFrame("tasklistframe.csv");
    }

    private void saveTasksToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Titre,Description,Date d'échéance,Priorité\n");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            for (Task task : tasks) {
                writer.write(task.getTitle() + "," +
                        task.getDescription() + "," +
                        dateFormat.format(task.getDeadline()) + "," +
                        task.getPriority() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement des tâches dans " + filename + ".");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TaskManagerFrame::new);
    }
}

class Task {
    private String title;
    private String description;
    private Date deadline;
    private String priority;

    public Task(String title, String description, Date deadline, String priority) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public String getPriority() {
        return priority;
    }
}
