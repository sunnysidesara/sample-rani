import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI for a logged-in Admin: create/manage/delete users, view all requests,
 * and view a specific resident's request history.
 */
public class AdminMenuFrame extends JFrame {
    private final Admin admin;
    private final FileManager fileManager = new FileManager();
    private final RequestManager requestManager = new RequestManager(fileManager);

    private final DefaultTableModel userTableModel =
            new DefaultTableModel(new Object[]{"Role", "Name", "ID", "Extra Info"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable userTable = new JTable(userTableModel);
    private final JTextField userSearchField = new JTextField(14);

    private final DefaultTableModel requestTableModel =
            new DefaultTableModel(new Object[]{"Request ID", "Resident ID", "Type", "Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable requestTable = new JTable(requestTableModel);
    private final JTextField requestSearchField = new JTextField(14);

    public AdminMenuFrame(Admin admin) {
        super("Admin Menu - " + admin.getFullName());
        this.admin = admin;
        buildUI();
        refreshUserTable();
        refreshRequestTable(null);
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(820, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(240, 243, 247));

        JLabel header = new JLabel("Welcome, " + admin.getFullName() + "  (Admin ID: " + admin.getAdminId() + ")");
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        header.setForeground(new Color(27, 42, 67));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(245, 247, 250));

        userTable.setRowHeight(28);
        userTable.setGridColor(new Color(220, 225, 232));
        userTable.setSelectionBackground(new Color(206, 225, 255));
        userTable.setForeground(Color.BLACK);
        userTable.getTableHeader().setBackground(new Color(45, 98, 219));
        userTable.getTableHeader().setForeground(Color.BLACK);
        userTable.getTableHeader().setFont(userTable.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));

        requestTable.setRowHeight(28);
        requestTable.setGridColor(new Color(220, 225, 232));
        requestTable.setSelectionBackground(new Color(206, 225, 255));
        requestTable.setForeground(Color.BLACK);
        requestTable.getTableHeader().setBackground(new Color(45, 98, 219));
        requestTable.getTableHeader().setForeground(Color.BLACK);
        requestTable.getTableHeader().setFont(requestTable.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));

        JPanel userPanel = new JPanel(new BorderLayout(6, 6));
        userPanel.setBackground(new Color(240, 243, 247));
        JPanel userSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userSearchPanel.setBackground(new Color(240, 243, 247));
        userSearchPanel.add(new JLabel("Search (Name / Role / UserID):"));
        JButton searchUsersBtn = new JButton("Search");
        JButton clearUserSearchBtn = new JButton("Clear");
        JButtonStyle.applySecondary(searchUsersBtn);
        JButtonStyle.applySecondary(clearUserSearchBtn);
        userSearchPanel.add(userSearchField);
        userSearchPanel.add(searchUsersBtn);
        userSearchPanel.add(clearUserSearchBtn);
        userPanel.add(userSearchPanel, BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        JPanel userButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userButtons.setBackground(new Color(240, 243, 247));
        JButton createBtn = new JButton("Create User");
        JButton editBtn = new JButton("Manage (Edit) Selected");
        JButton historyBtn = new JButton("View History of Selected");
        JButtonStyle.applySecondary(createBtn);
        JButtonStyle.applySecondary(editBtn);
        JButtonStyle.applySecondary(historyBtn);
        userButtons.add(createBtn); userButtons.add(editBtn); userButtons.add(historyBtn);
        userPanel.add(userButtons, BorderLayout.SOUTH);
        tabs.addTab("Users", userPanel);

        JPanel reqPanel = new JPanel(new BorderLayout(6, 6));
        reqPanel.setBackground(new Color(240, 243, 247));
        JPanel requestSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        requestSearchPanel.setBackground(new Color(240, 243, 247));
        requestSearchPanel.add(new JLabel("Search (Request ID / Resident ID / Status):"));
        JButton searchRequestsBtn = new JButton("Search");
        JButton clearRequestSearchBtn = new JButton("Clear");
        JButtonStyle.applySecondary(searchRequestsBtn);
        JButtonStyle.applySecondary(clearRequestSearchBtn);
        requestSearchPanel.add(requestSearchField);
        requestSearchPanel.add(searchRequestsBtn);
        requestSearchPanel.add(clearRequestSearchBtn);
        reqPanel.add(requestSearchPanel, BorderLayout.NORTH);
        reqPanel.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        JPanel reqButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reqButtons.setBackground(new Color(240, 243, 247));
        JButton refreshReqBtn = new JButton("Refresh All Requests");
        JButton viewRequestDetailsBtn = new JButton("View Details");
        JButton deleteRequestBtn = new JButton("Delete Selected Request");
        JButtonStyle.applySecondary(refreshReqBtn);
        JButtonStyle.applySecondary(viewRequestDetailsBtn);
        JButtonStyle.applySecondary(deleteRequestBtn);
        reqButtons.add(refreshReqBtn);
        reqButtons.add(viewRequestDetailsBtn);
        reqButtons.add(deleteRequestBtn);
        reqPanel.add(reqButtons, BorderLayout.SOUTH);
        tabs.addTab("All Requests", reqPanel);

        add(tabs, BorderLayout.CENTER);

        JButton logoutBtn = new JButton("Logout");
        JButtonStyle.applySecondary(logoutBtn);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(new Color(240, 243, 247));
        bottom.add(logoutBtn);
        add(bottom, BorderLayout.SOUTH);

        createBtn.addActionListener(e -> createUserDialog());
        editBtn.addActionListener(e -> editSelectedUser());
        historyBtn.addActionListener(e -> { admin.viewUserHistory(); viewSelectedUserHistory(); });
        searchUsersBtn.addActionListener(e -> refreshUserTable(userSearchField.getText().trim()));
        userSearchField.addActionListener(e -> refreshUserTable(userSearchField.getText().trim()));
        clearUserSearchBtn.addActionListener(e -> {
            userSearchField.setText("");
            refreshUserTable("");
        });
        refreshReqBtn.addActionListener(e -> {
            admin.viewAllRequest();
            refreshRequestTable(requestSearchField.getText().trim());
        });
        searchRequestsBtn.addActionListener(e -> refreshRequestTable(requestSearchField.getText().trim()));
        requestSearchField.addActionListener(e -> refreshRequestTable(requestSearchField.getText().trim()));
        clearRequestSearchBtn.addActionListener(e -> {
            requestSearchField.setText("");
            refreshRequestTable("");
        });
        viewRequestDetailsBtn.addActionListener(e -> viewSelectedRequestDetails());
        deleteRequestBtn.addActionListener(e -> deleteSelectedRequest());
        logoutBtn.addActionListener(e -> {
            admin.logout();
            dispose();
            new LoginFrame(fileManager).setVisible(true);
        });
    }

    private void refreshUserTable() {
        refreshUserTable(userSearchField.getText().trim());
    }

    private void refreshUserTable(String searchTerm) {
        userTableModel.setRowCount(0);
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        for (User u : fileManager.readUsers()) {
            String id, extra;
            if (u instanceof Resident r) {
                id = r.getResidentId(); extra = r.getAddress() + " / " + r.getContactNo();
            } else if (u instanceof Admin a) {
                id = a.getAdminId(); extra = "-";
            } else if (u instanceof Staff s) {
                id = s.getStaffId(); extra = "Handles: " + s.getAssignedType();
            } else {
                id = "-"; extra = "-";
            }
            String searchable = (u.getRole() + " " + u.getFullName() + " " + id + " " + extra).toLowerCase();
            if (!query.isEmpty() && !searchable.contains(query)) continue;
            userTableModel.addRow(new Object[]{u.getRole(), u.getFullName(), id, extra});
        }
    }

    private void refreshRequestTable(String searchTerm) {
        requestTableModel.setRowCount(0);
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        for (AssistanceRequest r : requestManager.getAllRequests()) {
            if (!query.isEmpty()
                    && !r.getRequestId().toLowerCase().contains(query)
                    && !r.getResidentId().toLowerCase().contains(query)
                    && !r.getStatus().toLowerCase().contains(query)) continue;
            requestTableModel.addRow(new Object[]{r.getRequestId(), r.getResidentId(), r.getType(),
                    r.getStatus()});
        }
    }

    private void viewSelectedRequestDetails() {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String requestId = (String) requestTableModel.getValueAt(row, 0);
        AssistanceRequest request = requestManager.findRequest(requestId);
        if (request == null) return;

        JPanel detailFields = new JPanel(new GridLayout(0, 2, 8, 6));
        addRequestDetailRow(detailFields, "Request ID", request.getRequestId());
        addRequestDetailRow(detailFields, "Resident ID", request.getResidentId());
        addRequestDetailRow(detailFields, "Type", request.getType());
        addRequestDetailRow(detailFields, "Time needed", request.getTimeNeeded().format(AssistanceRequest.DT_FMT));
        addRequestDetailRow(detailFields, "Additional details", request.getAdditionalDetails());
        addRequestDetailRow(detailFields, "Current status", request.getStatus());
        addRequestDetailRow(detailFields, "Submitted", request.getDateRequested().format(AssistanceRequest.DT_FMT));

        if (request instanceof MedicalRequest medical) {
            addRequestDetailRow(detailFields, "Location", request.getLocation());
            int itemNumber = 1;
            for (MedicalItem item : medical.getMedicalItems()) {
                addRequestDetailRow(detailFields, "Medical item " + itemNumber++, item.toString());
            }
        } else if (request instanceof FoodRequest food) {
            addRequestDetailRow(detailFields, "Location", request.getLocation());
            int itemNumber = 1;
            for (FoodItem item : food.getFoodItems()) {
                addRequestDetailRow(detailFields, "Food item " + itemNumber++, item.toString());
            }
        } else if (request instanceof TransportationRequest transportation) {
            addRequestDetailRow(detailFields, "Pickup location", transportation.getPickupLocation());
            addRequestDetailRow(detailFields, "Destination", transportation.getDestination());
            addRequestDetailRow(detailFields, "Passengers", String.valueOf(transportation.getPassengerCount()));
        }

        DefaultTableModel historyModel = new DefaultTableModel(
                new Object[]{"Staff ID", "Status", "Updated At"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (StaffRequestHistory entry : fileManager.readStaffRequestHistory()) {
            if (entry.getRequestId().equals(requestId)) {
                historyModel.addRow(new Object[]{entry.getStaffId(), entry.getStatus(),
                        entry.getProcessedAt().format(AssistanceRequest.DT_FMT)});
            }
        }

        DefaultTableModel residentHistoryModel = new DefaultTableModel(
                new Object[]{"Resident ID", "Action", "Request Snapshot", "Updated At"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (ResidentRequestHistory entry : fileManager.readResidentRequestHistory()) {
            if (entry.getRequestId().equals(requestId)) {
                residentHistoryModel.addRow(new Object[]{entry.getResidentId(), entry.getAction(),
                        entry.getRequestSummary(), entry.getUpdatedAt().format(AssistanceRequest.DT_FMT)});
            }
        }

        JPanel content = new JPanel(new BorderLayout(8, 10));
        content.add(detailFields, BorderLayout.NORTH);
        JTabbedPane historyTabs = new JTabbedPane();
        historyTabs.addTab("Staff Status Changes", new JScrollPane(new JTable(historyModel)));
        historyTabs.addTab("Resident Updates", new JScrollPane(new JTable(residentHistoryModel)));
        historyTabs.setPreferredSize(new Dimension(700, 180));
        content.add(historyTabs, BorderLayout.CENTER);
        content.setPreferredSize(new Dimension(700, 440));
        JOptionPane.showMessageDialog(this, new JScrollPane(content),
                "Request Details - " + requestId, JOptionPane.PLAIN_MESSAGE);
    }

    private void addRequestDetailRow(JPanel panel, String label, String value) {
        panel.add(new JLabel(label + ":"));
        JTextArea valueArea = new JTextArea(value == null ? "" : value);
        valueArea.setEditable(false);
        valueArea.setLineWrap(true);
        valueArea.setWrapStyleWord(true);
        valueArea.setRows(Math.max(1, Math.min(4, (valueArea.getText().length() / 45) + 1)));
        valueArea.setBackground(panel.getBackground());
        valueArea.setBorder(BorderFactory.createEmptyBorder());
        panel.add(valueArea);
    }

    private void deleteSelectedRequest() {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String requestId = (String) requestTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Permanently delete request " + requestId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            requestManager.deleteRequest(requestId);
            refreshRequestTable(requestSearchField.getText().trim());
        }
    }

    private User getSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String name = (String) userTableModel.getValueAt(row, 1);
        for (User u : fileManager.readUsers()) {
            if (u.getFullName().equals(name)) return u;
        }
        return null;
    }

    private void createUserDialog() {
        String[] roles = {"Resident", "Admin", "MedicalStaff", "FoodStaff", "TransportationStaff"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        JTextField nameField = new JTextField();
        JTextField passField = new JTextField();
        JTextField extra1 = new JTextField(); // address (Resident)
        JTextField extra2 = new JTextField(); // contact (Resident)

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Role:")); panel.add(roleBox);
        panel.add(new JLabel("Name:")); panel.add(nameField);
        panel.add(new JLabel("Password:")); panel.add(passField);
        panel.add(new JLabel("Address (Resident only):")); panel.add(extra1);
        panel.add(new JLabel("Contact No (Resident only):")); panel.add(extra2);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String role = (String) roleBox.getSelectedItem();
        String name = nameField.getText().trim();
        String pass = passField.getText().trim();
        if (name.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and password are required.");
            return;
        }

        User newUser;
        switch (role) {
            case "Resident":
                String contact = extra2.getText().trim();
                if (!contact.matches("\\+?[0-9]+")) {
                    JOptionPane.showMessageDialog(this, "Contact number must be numeric.",
                            "Invalid Input", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                newUser = new Resident(name, pass, fileManager.nextResidentId(), extra1.getText().trim(), contact);
                break;
            case "Admin":
                newUser = new Admin(name, pass, "ADM" + System.currentTimeMillis());
                break;
            case "MedicalStaff":
                newUser = new MedicalStaff(name, pass, "STF" + System.currentTimeMillis());
                break;
            case "FoodStaff":
                newUser = new FoodStaff(name, pass, "STF" + System.currentTimeMillis());
                break;
            default:
                newUser = new TransportationStaff(name, pass, "STF" + System.currentTimeMillis());
                break;
        }

        admin.createUser(newUser);
        refreshUserTable();
        JOptionPane.showMessageDialog(this, "User created.");
    }

    private void editSelectedUser() {
        User u = getSelectedUser();
        if (u == null) return;

        String oldUserId = getUserId(u);
        admin.manageUser(oldUserId);
        String[] roles = {"Resident", "Admin", "MedicalStaff", "FoodStaff", "TransportationStaff"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setSelectedItem(u.getRole());
        JTextField nameField = new JTextField(u.getFullName());
        JTextField passwordField = new JTextField(u.getPassword());
        JTextField userIdField = new JTextField(oldUserId);
        JTextField addressField = new JTextField(u instanceof Resident resident ? resident.getAddress() : "");
        JTextField contactField = new JTextField(u instanceof Resident resident
                ? String.valueOf(resident.getContactNo()) : "");
        JComboBox<String> assignedTypeBox = new JComboBox<>(new String[]{"Medical", "Food", "Transportation"});
        if (u instanceof Staff staff) assignedTypeBox.setSelectedItem(staff.getAssignedType());
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        Runnable rebuildFields = () -> {
            String selectedRole = (String) roleBox.getSelectedItem();
            panel.removeAll();
            panel.add(new JLabel("Role:"));
            panel.add(roleBox);
            panel.add(new JLabel("Name:"));
            panel.add(nameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
            panel.add(new JLabel("UserID:"));
            panel.add(userIdField);

            if ("Resident".equals(selectedRole)) {
            panel.add(new JLabel("Address:"));
            panel.add(addressField);
            panel.add(new JLabel("Contact No:"));
            panel.add(contactField);
            } else if (selectedRole != null && selectedRole.endsWith("Staff")) {
            panel.add(new JLabel("Assigned Type:"));
            panel.add(assignedTypeBox);
            }
            panel.revalidate();
            panel.repaint();
        };
        roleBox.addActionListener(e -> {
            String selectedRole = (String) roleBox.getSelectedItem();
            userIdField.setText(createUserIdForRole(selectedRole));
            if ("MedicalStaff".equals(selectedRole)) assignedTypeBox.setSelectedItem("Medical");
            else if ("FoodStaff".equals(selectedRole)) assignedTypeBox.setSelectedItem("Food");
            else if ("TransportationStaff".equals(selectedRole)) assignedTypeBox.setSelectedItem("Transportation");
            rebuildFields.run();
        });
        rebuildFields.run();

        JScrollPane manageScroll = new JScrollPane(panel);
        manageScroll.setPreferredSize(new Dimension(700, 280));
        Object[] options = {"Save Changes", "Delete User", "Cancel"};
        int result = JOptionPane.showOptionDialog(this, manageScroll, "Manage User",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (result == 1) {
            deleteUser(u);
            return;
        }
        if (result != 0) return;

        String newName = nameField.getText().trim();
        String newPassword = passwordField.getText().trim();
        String newUserId = userIdField.getText().trim();
        String newRole = (String) roleBox.getSelectedItem();
        if (newName.isEmpty() || newPassword.isEmpty() || newUserId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, password, and UserID are required.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String requiredPrefix = getUserIdPrefix(newRole);
        if (!newUserId.toUpperCase().startsWith(requiredPrefix)) {
            JOptionPane.showMessageDialog(this, "The UserID for " + newRole + " must start with "
                    + requiredPrefix + ".", "Invalid UserID", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (User existingUser : fileManager.readUsers()) {
            if (!getUserId(existingUser).equalsIgnoreCase(oldUserId)
                    && getUserId(existingUser).equalsIgnoreCase(newUserId)) {
                JOptionPane.showMessageDialog(this, "That UserID is already in use.",
                        "Duplicate UserID", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        User updatedUser;
        if ("Resident".equals(newRole)) {
            String contactNo = contactField.getText().trim();
            if (!contactNo.matches("\\+?[0-9]+")) {
                JOptionPane.showMessageDialog(this, "Contact number must be numeric.",
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            updatedUser = new Resident(newName, newPassword, newUserId, addressField.getText().trim(), contactNo);
        } else if ("Admin".equals(newRole)) {
            updatedUser = new Admin(newName, newPassword, newUserId);
        } else if ("MedicalStaff".equals(newRole)) {
            updatedUser = new MedicalStaff(newName, newPassword, newUserId);
        } else if ("FoodStaff".equals(newRole)) {
            updatedUser = new FoodStaff(newName, newPassword, newUserId);
        } else {
            updatedUser = new TransportationStaff(newName, newPassword, newUserId);
        }
        if (updatedUser instanceof Staff updatedStaff) {
            updatedStaff.setAssignedType((String) assignedTypeBox.getSelectedItem());
        }

        if (u instanceof Resident && !oldUserId.equals(newUserId)) {
            fileManager.updateResidentIdInRequests(oldUserId, newUserId);
        }
        if (u instanceof Staff && updatedUser instanceof Staff && !oldUserId.equals(newUserId)) {
            fileManager.updateStaffIdInHistory(oldUserId, newUserId);
        }

        List<User> all = fileManager.readUsers();
        for (int i = 0; i < all.size(); i++) {
            if (getUserId(all.get(i)).equals(oldUserId)) {
                all.set(i, updatedUser);
                break;
            }
        }
        fileManager.saveAllUsers(all);
        refreshUserTable();
        JOptionPane.showMessageDialog(this, "User updated as " + newRole + ".");
    }

    private String getUserIdPrefix(String role) {
        if ("Resident".equals(role)) return "RSD";
        if ("Admin".equals(role)) return "ADM";
        return "STF";
    }

    private String createUserIdForRole(String role) {
        if ("Resident".equals(role)) return fileManager.nextResidentId();
        return getUserIdPrefix(role) + System.currentTimeMillis();
    }

    private String getUserId(User user) {
        if (user instanceof Resident resident) return resident.getResidentId();
        if (user instanceof Admin adminUser) return adminUser.getAdminId();
        if (user instanceof Staff staff) return staff.getStaffId();
        return "";
    }

    private void deleteUser(User u) {
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user " + u.getFullName() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String userId = getUserId(u);
        admin.deleteUser(userId);
        refreshUserTable();
    }

    private void viewSelectedUserHistory() {
        User u = getSelectedUser();
        if (u == null) return;
        if (u instanceof Staff staff) {
            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Request ID", "Resident ID", "Type", "Status", "Processed At"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (StaffRequestHistory entry : fileManager.readStaffRequestHistory()) {
                if (entry.getStaffId().equals(staff.getStaffId())) {
                    model.addRow(new Object[]{entry.getRequestId(), entry.getResidentId(), entry.getRequestType(),
                            entry.getStatus(), entry.getProcessedAt().format(AssistanceRequest.DT_FMT)});
                }
            }
            JTable historyTable = new JTable(model);
            JScrollPane scroll = new JScrollPane(historyTable);
            scroll.setPreferredSize(new Dimension(700, 280));
            JOptionPane.showMessageDialog(this, scroll, "Requests handled by " + staff.getFullName(),
                    JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if (!(u instanceof Resident r)) {
            JOptionPane.showMessageDialog(this, "History is available for Resident and Staff accounts.",
                    "Not Applicable", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Request ID", "Type", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (AssistanceRequest req : requestManager.getAllRequests()) {
            if (req.getResidentId().equals(r.getResidentId())) {
            model.addRow(new Object[]{req.getRequestId(), req.getType(), req.getStatus()});
            }
        }
        JTable historyTable = new JTable(model);
        DefaultTableModel updatesModel = new DefaultTableModel(
                new Object[]{"Request ID", "Action", "Request Snapshot", "Updated At"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (ResidentRequestHistory entry : fileManager.readResidentRequestHistory()) {
            if (entry.getResidentId().equals(r.getResidentId())) {
                updatesModel.addRow(new Object[]{entry.getRequestId(), entry.getAction(),
                        entry.getRequestSummary(), entry.getUpdatedAt().format(AssistanceRequest.DT_FMT)});
            }
        }

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Requests", new JScrollPane(historyTable));
        tabs.addTab("Request Updates", new JScrollPane(new JTable(updatesModel)));
        tabs.setPreferredSize(new Dimension(700, 320));
        JOptionPane.showMessageDialog(this, tabs, "History for " + r.getFullName(), JOptionPane.PLAIN_MESSAGE);
    }
}
