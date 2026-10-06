import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * GUI shared by MedicalStaff, FoodStaff, and TransportationStaff: view, search,
 * and update the status of requests matching their assigned type.
 */
public class StaffMenuFrame extends JFrame {
    private static final Dimension REQUEST_DIALOG_SIZE = new Dimension(560, 390);
    private final Staff staff;
    private final FileManager fileManager = new FileManager();
    private final RequestManager requestManager = new RequestManager(fileManager);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Request ID", "Resident ID", "Details", "Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(14);

    public StaffMenuFrame(Staff staff) {
        super(staff.getRole() + " Menu - " + staff.getFullName() + " (" + staff.getAssignedType() + ")");
        this.staff = staff;
        buildUI();
        refreshTable(null);
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(240, 243, 247));

        JLabel header = new JLabel("Welcome, " + staff.getFullName() + "  (Staff ID: " + staff.getStaffId()
                + ", assigned: " + staff.getAssignedType() + ")");
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        header.setForeground(new Color(27, 42, 67));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(new Color(245, 247, 250));
        top.add(new JLabel("Search (Request ID / Resident ID / Status):"));
        searchField.setPreferredSize(new Dimension(220, 28));
        top.add(searchField);
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Clear");
        JButtonStyle.applySecondary(searchBtn);
        JButtonStyle.applySecondary(clearBtn);
        top.add(searchBtn);
        top.add(clearBtn);

        JPanel heading = new JPanel(new BorderLayout());
        heading.setBackground(new Color(245, 247, 250));
        heading.add(header, BorderLayout.NORTH);
        heading.add(top, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);

        table.setRowHeight(28);
        table.setGridColor(new Color(220, 225, 232));
        table.setSelectionBackground(new Color(206, 225, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setForeground(Color.BLACK);
        table.getTableHeader().setBackground(new Color(45, 98, 219));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(new Color(240, 243, 247));
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(new Color(240, 243, 247));
        JButton refreshBtn = new JButton("View Request");
        JButton updateStatusBtn = new JButton("Update Status");
        JButton logoutBtn = new JButton("Logout");
        JButtonStyle.applySecondary(refreshBtn);
        JButtonStyle.applyAccent(updateStatusBtn);
        JButtonStyle.applySecondary(logoutBtn);
        bottom.add(refreshBtn);
        bottom.add(updateStatusBtn);
        logoutPanel.add(logoutBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(new Color(240, 243, 247));
        south.add(bottom, BorderLayout.NORTH);
        south.add(logoutPanel, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> { staff.viewRequests(); showSelectedRequestDetails(); });
        searchBtn.addActionListener(e -> { staff.searchRequest(); refreshTable(searchField.getText().trim()); });
        clearBtn.addActionListener(e -> { searchField.setText(""); refreshTable(null); });
        updateStatusBtn.addActionListener(e -> { staff.updateRequestStatus(); updateStatusDialog(); });
        logoutBtn.addActionListener(e -> {
            staff.logout();
            dispose();
            new LoginFrame(fileManager).setVisible(true);
        });
    }

    private void refreshTable(String searchTerm) {
        tableModel.setRowCount(0);
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        List<AssistanceRequest> all = requestManager.getAllRequests();
        for (AssistanceRequest r : all) {
            if (!r.getType().equalsIgnoreCase(staff.getAssignedType())) continue;
            if (!query.isEmpty()
                    && !r.getRequestId().toLowerCase().contains(query)
                    && !r.getResidentId().toLowerCase().contains(query)
                    && !r.getStatus().toLowerCase().contains(query)) {
                continue;
            }
            tableModel.addRow(new Object[]{r.getRequestId(), r.getResidentId(), r.getDetails(), r.getStatus()});
        }
    }

    private AssistanceRequest getSelectedRequest() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        return requestManager.findRequest(id);
    }

    private void showSelectedRequestDetails() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        addDetailRow(detailsPanel, "Request ID", req.getRequestId());
        addDetailRow(detailsPanel, "Resident ID", req.getResidentId());
        addDetailRow(detailsPanel, "Type", req.getType());
        addDetailRow(detailsPanel, "Details", req.getDetails());
        addDetailRow(detailsPanel, "Status", req.getStatus());
        addDetailRow(detailsPanel, "Submitted", req.getDateRequested().format(AssistanceRequest.DT_FMT));

        if (req instanceof MedicalRequest medical) {
            addDetailRow(detailsPanel, "Medical item", medical.getMedicalItem());
            addDetailRow(detailsPanel, "Quantity", medical.getQuantity());
        } else if (req instanceof FoodRequest food) {
            addDetailRow(detailsPanel, "Food items", food.getFoodItems());
            addDetailRow(detailsPanel, "Quantity", food.getQuantity());
        } else if (req instanceof TransportationRequest transportation) {
            addDetailRow(detailsPanel, "Pickup location", transportation.getPickupLocation());
            addDetailRow(detailsPanel, "Destination", transportation.getDestination());
            addDetailRow(detailsPanel, "Needed at", transportation.getWhenNeeded().format(AssistanceRequest.DT_FMT));
        }

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setPreferredSize(new Dimension(520, 320));
        Object[] options = {"Change Status", "Close"};
        int choice = showRequestDialog(scrollPane, "Request Details", options);
        if (choice == 0) updateStatusDialog(req);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
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

    private void updateStatusDialog() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;
        updateStatusDialog(req);
    }

    private void updateStatusDialog(AssistanceRequest req) {
        String[] statuses = {"Pending", "In Progress", "Help is OTW", "Completed", "Cancelled"};
        JComboBox<String> statusBox = new JComboBox<>(statuses);

        String currentStatus = req.getStatus() == null ? "Pending" : req.getStatus().trim();
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(currentStatus)) {
                statusBox.setSelectedIndex(i);
                break;
            }
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Request ID:"));
        panel.add(new JLabel(req.getRequestId()));
        panel.add(new JLabel("Resident ID:"));
        panel.add(new JLabel(req.getResidentId()));
        panel.add(new JLabel("Current status:"));
        panel.add(new JLabel(currentStatus));
        panel.add(new JLabel("New status:"));
        panel.add(statusBox);

        Object[] options = {"Save Status", "Close"};
        int choice = showRequestDialog(panel, "Update Request", options);
        if (choice != 0) return;

        String newStatus = (String) statusBox.getSelectedItem();
        req.updateStatus(newStatus);
        fileManager.recordStaffRequestAction(staff, req, newStatus);
        requestManager.updateRequest(req.getRequestId());
        refreshTable(searchField.getText().trim());
        JOptionPane.showMessageDialog(this, "Status updated to " + newStatus + ".");
    }

    private int showRequestDialog(Component content, String title, Object[] options) {
        JOptionPane optionPane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        JDialog dialog = optionPane.createDialog(this, title);
        dialog.setResizable(true);
        dialog.setSize(REQUEST_DIALOG_SIZE);
        dialog.setMinimumSize(REQUEST_DIALOG_SIZE);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        Object selectedOption = optionPane.getValue();
        dialog.dispose();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selectedOption)) return i;
        }
        return -1;
    }

}
