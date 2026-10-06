import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Login window shared by every role. Also offers resident self-registration.
 */
public class LoginFrame extends JFrame {
    private final FileManager fileManager;
    private final JTextField userIdField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);

    public LoginFrame(FileManager fileManager) {
        super("Barangay Assistance System - Login");
        this.fileManager = fileManager;
        buildUI();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(241, 245, 250));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 245, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Barangay Assistance System", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(27, 42, 67));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(title, c);

        userIdField.setPreferredSize(new Dimension(250, 32));
        userIdField.setBackground(Color.WHITE);
        userIdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(188, 204, 224), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        passwordField.setPreferredSize(new Dimension(250, 32));
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(188, 204, 224), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; panel.add(new JLabel("UserID:"), c);
        c.gridx = 1; panel.add(userIdField, c);

        c.gridy = 2; c.gridx = 0; panel.add(new JLabel("Password:"), c);
        c.gridx = 1; panel.add(passwordField, c);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(44, 120, 230));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginBtn.setFont(loginBtn.getFont().deriveFont(Font.BOLD, 13f));

        c.gridy = 3; c.gridx = 0; c.gridwidth = 2; panel.add(loginBtn, c);

        add(panel);

        loginBtn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String userId = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (userId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both UserID and password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<User> users = fileManager.readUsers();
        for (User u : users) {
            String storedUserId = getUserId(u);
            if (storedUserId != null && storedUserId.equalsIgnoreCase(userId)
                    && hasValidPrefix(storedUserId) && u.authenticate(password) && u.login()) {
                dispose();
                u.showMenu();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Invalid UserID or password.",
                "Login Failed", JOptionPane.ERROR_MESSAGE);
    }

    private String getUserId(User user) {
        if (user instanceof Admin) return ((Admin) user).getAdminId();
        if (user instanceof Staff) return ((Staff) user).getStaffId();
        if (user instanceof Resident) return ((Resident) user).getResidentId();
        return null;
    }

    private boolean hasValidPrefix(String userId) {
        String normalizedId = userId.toUpperCase();
        return normalizedId.startsWith("ADM")
            || normalizedId.startsWith("STF")
            || normalizedId.startsWith("RSD")
            || normalizedId.startsWith("RES");
    }
}
