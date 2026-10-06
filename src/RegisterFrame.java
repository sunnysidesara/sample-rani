import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Lets a new resident create their own account (a convenience on top of the
 * Admin's createUser() capability).
 */
public class RegisterFrame extends JFrame {
    private final FileManager fileManager;
    private final JFrame loginFrame;

    private final JTextField nameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JTextField addressField = new JTextField(16);
    private final JTextField contactField = new JTextField(16);

    public RegisterFrame(FileManager fileManager, JFrame loginFrame) {
        super("Resident Registration");
        this.fileManager = fileManager;
        this.loginFrame = loginFrame;
        buildUI();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(360, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        c.gridy = row++; c.gridx = 0; panel.add(new JLabel("Full Name:"), c);
        c.gridx = 1; panel.add(nameField, c);

        c.gridy = row++; c.gridx = 0; panel.add(new JLabel("Password:"), c);
        c.gridx = 1; panel.add(passwordField, c);

        c.gridy = row++; c.gridx = 0; panel.add(new JLabel("Address:"), c);
        c.gridx = 1; panel.add(addressField, c);

        c.gridy = row++; c.gridx = 0; panel.add(new JLabel("Contact No:"), c);
        c.gridx = 1; panel.add(contactField, c);

        JButton submit = new JButton("Create Account");
        JButton back = new JButton("Back to Login");
        c.gridy = row++; c.gridx = 0; panel.add(back, c);
        c.gridx = 1; panel.add(submit, c);

        add(panel);

        submit.addActionListener(e -> register());
        back.addActionListener(e -> {
            dispose();
            loginFrame.setVisible(true);
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                loginFrame.setVisible(true);
            }
        });
    }

    private void register() {
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String address = addressField.getText().trim();
        String contactText = contactField.getText().trim();

        if (name.isEmpty() || password.isEmpty() || address.isEmpty() || contactText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!contactText.matches("\\+?[0-9]+")) {
            JOptionPane.showMessageDialog(this, "Contact number must be numeric.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<User> existing = fileManager.readUsers();
        for (User u : existing) {
            if (u.getFullName().equalsIgnoreCase(name)) {
                JOptionPane.showMessageDialog(this, "That name is already registered.",
                        "Duplicate Account", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String residentId = fileManager.nextResidentId();
        Resident resident = new Resident(name, password, residentId, address, contactText);
        fileManager.saveUser(resident);

        JOptionPane.showMessageDialog(this, "Account created! You may now log in.",
                "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        loginFrame.setVisible(true);
    }
}
