package ui;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskListFrame extends JFrame {
    private DefaultTableModel model;
    private JTable tasksTable;
    private JTextField searchField;
    private JButton notificationsButton; // Référence au bouton de notifications

    public TaskListFrame(String filename) {
        super("Liste des tâches");

        // Initialisation du modèle de données
        initializeModel();

        // Chargement des tâches depuis le fichier CSV
        loadTasksFromFile(filename);

        // Initialisation de l'interface graphique
        initializeUI();

        // Vérifier l'état des notifications au démarrage de l'application
        checkNotificationsStatus();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeModel() {
        String[] columnNames = {"Titre", "Description", "Date d'échéance", "Priorité", "Statut"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: // Date d'échéance
                        return Date.class;
                    case 3: // Priorité
                        return String.class; // Modifier pour enregistrer la priorité en tant que chaîne de caractères
                    default:
                        return String.class;
                }
            }
        };
    }

    private void loadTasksFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    Date deadline = parseDate(parts[2]);
                    model.addRow(new Object[]{parts[0], parts[1], deadline, parts[3], "En cours"}); // Ajout direct de la priorité
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des tâches depuis " + filename + ".");
        }
    }

    private Date parseDate(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private void initializeUI() {
        tasksTable = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                Object value = getModel().getValueAt(row, 4); // Récupérer la valeur de la colonne "Statut"
                if (value != null && value.toString().equals("Terminée")) {
                    comp.setBackground(Color.GREEN); // Colorer en vert si la tâche est terminée
                } else {
                    comp.setBackground(getBackground());
                }
                return comp;
            }
        };
        tasksTable.setRowHeight(30); // Définir la hauteur des lignes
        JScrollPane scrollPane = new JScrollPane(tasksTable);
        getContentPane().add(scrollPane);

        // Ajouter un champ de recherche en haut de la table
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Rechercher");
        searchButton.addActionListener(e -> searchTasks());
        searchPanel.add(new JLabel("Rechercher par titre ou date d'échéance:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        getContentPane().add(searchPanel, "North");

        // Ajouter des boutons pour la suppression et le marquage
        JButton deleteButton = new JButton("Supprimer");
        deleteButton.addActionListener(e -> {
            int selectedRow = tasksTable.getSelectedRow();
            if (selectedRow != -1) {
                model.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une tâche à supprimer.");
            }
        });

        JButton markAsDoneButton = new JButton("Marquer comme terminée");
        markAsDoneButton.addActionListener(e -> markTaskAsDone());

        notificationsButton = new JButton("Notifications");
        notificationsButton.addActionListener(e -> showNotificationsInterface());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(deleteButton);
        buttonPanel.add(markAsDoneButton);
        buttonPanel.add(notificationsButton);
        getContentPane().add(buttonPanel, "South");

        // Ajouter le tri par ordre croissant/décroissant
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tasksTable.setRowSorter(sorter);
        sorter.setComparator(2, (o1, o2) -> {
            if (o1 instanceof Date && o2 instanceof Date) {
                return ((Date) o1).compareTo((Date) o2);
            }
            return 0;
        });
    }

    private void searchTasks() {
        String searchText = searchField.getText().trim();
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) tasksTable.getRowSorter();
        if (sorter != null) {
            RowFilter<DefaultTableModel, Object> filter = RowFilter.regexFilter("(?i)" + searchText, 0, 2);
            sorter.setRowFilter(filter);
        }
    }

    private void markTaskAsDone() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow != -1) {
            model.setValueAt("Terminée", selectedRow, 4); // Modifier le statut de la tâche sélectionnée
            // Actualiser l'affichage pour mettre à jour la couleur de la ligne
            tasksTable.repaint();
            // Vérifier l'état des notifications après marquage de tâche comme terminée
            checkNotificationsStatus();
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une tâche à marquer comme terminée.");
        }
    }

    private void showNotificationsInterface() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = new Date();
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000; // 7 jours en millisecondes
        Date deadlineLimit = new Date(currentDate.getTime() + sevenDaysInMillis);

        StringBuilder notificationsBuilder = new StringBuilder();
        boolean hasNotifications = false;
        for (int row = 0; row < model.getRowCount(); row++) {
            Date deadline = (Date) model.getValueAt(row, 2); // Colonne de la date d'échéance
            if (deadline != null && deadline.before(deadlineLimit)) {
                String title = (String) model.getValueAt(row, 0); // Colonne du titre
                if (!hasNotifications) {
                    notificationsBuilder.append("Tâche(s) à accomplir dans moins de 7 jours:\n");
                    hasNotifications = true;
                }
                notificationsBuilder.append("  - Tâche : ").append(title).append(" - Date d'échéance : ").append(dateFormat.format(deadline)).append("\n");
            }
        }

        if (!hasNotifications) {
            notificationsBuilder.append("Aucune notification : Tâches à accomplir dans moins de 7 jours.");
        }

        JOptionPane.showMessageDialog(this, notificationsBuilder.toString(), "Notifications", JOptionPane.INFORMATION_MESSAGE);
    }

    private void checkNotificationsStatus() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = new Date();
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000; // 7 jours en millisecondes
        Date deadlineLimit = new Date(currentDate.getTime() + sevenDaysInMillis);

        boolean hasCriticalTasks = false;
        for (int row = 0; row < model.getRowCount(); row++) {
            Date deadline = (Date) model.getValueAt(row, 2); // Colonne de la date d'échéance
            String status = (String) model.getValueAt(row, 4); // Colonne du statut
            if (deadline != null && deadline.before(deadlineLimit) && !status.equals("Terminée")) {
                hasCriticalTasks = true;
                break;
            }
        }

        // Modifier la couleur du bouton de notification en rouge s'il y a des tâches critiques
        if (hasCriticalTasks) {
            notificationsButton.setBackground(Color.RED);
        } else {
            notificationsButton.setBackground(null); // Rétablir la couleur par défaut
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TaskListFrame("tasklistframe.csv"));
    }
}
