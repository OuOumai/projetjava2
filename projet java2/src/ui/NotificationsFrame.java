package ui;

import javax.swing.*;
import java.awt.*;

public class NotificationsFrame extends JFrame {
    public NotificationsFrame(String[] messages, boolean hasNotifications) {
        super("Notifications");

        JPanel panel = new JPanel(new GridLayout(messages.length, 1));
        for (String message : messages) {
            JLabel label = new JLabel(message);
            panel.add(label);
        }

        if (!hasNotifications) {
            JLabel noNotificationLabel = new JLabel("Aucune notification.");
            panel.add(noNotificationLabel);
        }

        JScrollPane scrollPane = new JScrollPane(panel);
        getContentPane().add(scrollPane);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
