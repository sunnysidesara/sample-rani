import javax.swing.*;
import java.awt.*;

public final class JButtonStyle {
    private JButtonStyle() {
    }

    public static void applyAccent(JButton button) {
        button.setBackground(new Color(44, 120, 230));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
    }

    public static void applySecondary(JButton button) {
        button.setBackground(new Color(234, 238, 244));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 196, 210), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 12f));
    }
}
