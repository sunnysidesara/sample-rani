import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GUI for a logged-in Resident: submit / view / update / cancel their own requests.
 */
public class ResidentMenuFrame extends JFrame {
    private final Resident resident;
    private final FileManager fileManager = new FileManager();
    private final RequestManager requestManager = new RequestManager(fileManager);
    private static final DateTimeFormatter WHEN_NEEDED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Request ID", "Type", "Details", "Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    private final JTable table = new JTable(tableModel);
    private final JTextField requestSearchField = new JTextField(14);

    public ResidentMenuFrame(Resident resident) {
        super("Resident Menu - " + resident.getFullName());
        this.resident = resident;
        buildUI();
        refreshTable();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(240, 243, 247));

        JLabel header = new JLabel("Welcome, " + resident.getFullName() + "  (Resident ID: "
                + resident.getResidentId() + ")", SwingConstants.LEFT);
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        header.setForeground(new Color(27, 42, 67));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));
        JLabel editNote = new JLabel("Requests can be edited only while their status is Pending.");
        editNote.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        editNote.setForeground(new Color(75, 87, 101));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setBackground(new Color(245, 247, 250));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(245, 247, 250));
        searchPanel.add(new JLabel("Search (Request ID / Status):"));
        requestSearchField.setPreferredSize(new Dimension(180, 28));
        searchPanel.add(requestSearchField);
        JButton searchBtn = new JButton("Search");
        JButton clearSearchBtn = new JButton("Clear");
        JButtonStyle.applySecondary(searchBtn);
        JButtonStyle.applySecondary(clearSearchBtn);
        searchPanel.add(searchBtn);
        searchPanel.add(clearSearchBtn);
        heading.add(header, BorderLayout.NORTH);
        heading.add(editNote, BorderLayout.CENTER);
        heading.add(searchPanel, BorderLayout.SOUTH);
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

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(new Color(240, 243, 247));
        JButton submitBtn = new JButton("Submit Request");
        JButton refreshBtn = new JButton("Refresh");
        JButton updateBtn = new JButton("Update Selected");
        JButton logoutBtn = new JButton("Logout");
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(new Color(240, 243, 247));
        JButtonStyle.applyAccent(submitBtn);
        JButtonStyle.applySecondary(refreshBtn);
        JButtonStyle.applySecondary(updateBtn);
        JButtonStyle.applySecondary(logoutBtn);
        buttons.add(submitBtn);
        buttons.add(refreshBtn);
        buttons.add(updateBtn);
        logoutPanel.add(logoutBtn);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(240, 243, 247));
        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(logoutPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        submitBtn.addActionListener(e -> submitRequestDialog());
        refreshBtn.addActionListener(e -> { resident.viewMyRequest(); refreshTable(); });
        updateBtn.addActionListener(e -> updateSelectedDialog());
        searchBtn.addActionListener(e -> refreshTable(requestSearchField.getText().trim()));
        requestSearchField.addActionListener(e -> refreshTable(requestSearchField.getText().trim()));
        clearSearchBtn.addActionListener(e -> {
            requestSearchField.setText("");
            refreshTable("");
        });
        logoutBtn.addActionListener(e -> {
            resident.logout();
            dispose();
            new LoginFrame(fileManager).setVisible(true);
        });
    }

    private void refreshTable() {
        refreshTable(requestSearchField.getText().trim());
    }

    private void refreshTable(String searchTerm) {
        tableModel.setRowCount(0);
        String query = searchTerm == null ? "" : searchTerm.toLowerCase();
        List<AssistanceRequest> all = requestManager.getAllRequests();
        for (AssistanceRequest r : all) {
            if (!r.getResidentId().equals(resident.getResidentId())) continue;
            if (!query.isEmpty()
                    && !r.getRequestId().toLowerCase().contains(query)
                    && !r.getStatus().toLowerCase().contains(query)) continue;
            tableModel.addRow(new Object[]{r.getRequestId(), r.getType(), r.getDetails(), r.getStatus()});
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

    private void submitRequestDialog() {
        String[] types = {"Medical", "Food", "Transportation"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        RequestFormFields fields = new RequestFormFields();
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        Runnable rebuildForm = () -> {
            panel.removeAll();
            addFormRow(panel, "Type:", typeBox);
            addFormRow(panel, "Details:", fields.detailsField);
            addTypeFields(panel, (String) typeBox.getSelectedItem(), fields);
            panel.revalidate();
            panel.repaint();
        };
        typeBox.addActionListener(e -> rebuildForm.run());
        rebuildForm.run();

        JScrollPane formScroll = new JScrollPane(panel);
        formScroll.setPreferredSize(new Dimension(520, 230));
        int result = JOptionPane.showConfirmDialog(this, formScroll, "Submit Assistance Request",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String type = (String) typeBox.getSelectedItem();
        if (!hasRequiredFields(type, fields)) return;

        String details = fields.detailsField.getText().trim();
        String requestId = fileManager.nextRequestId();
        LocalDateTime now = LocalDateTime.now();

        AssistanceRequest newRequest;
        switch (type) {
            case "Medical":
                newRequest = new MedicalRequest(requestId, resident.getResidentId(), details, "Pending",
                        now, fields.itemField.getText().trim(), fields.quantityField.getText().trim());
                break;
            case "Food":
                newRequest = new FoodRequest(requestId, resident.getResidentId(), details, "Pending",
                        now, fields.itemField.getText().trim(), fields.quantityField.getText().trim());
                break;
            case "Transportation":
                LocalDateTime whenNeeded = parseWhenNeeded(fields.whenNeededField.getText().trim());
                newRequest = new TransportationRequest(requestId, resident.getResidentId(), details, "Pending",
                        now, fields.pickupField.getText().trim(), fields.destinationField.getText().trim(), whenNeeded);
                break;
            default:
                return;
        }

        resident.submitRequest(newRequest);
        JOptionPane.showMessageDialog(this, "Request submitted successfully.");
        refreshTable();
    }

    private void updateSelectedDialog() {
        AssistanceRequest req = getSelectedRequest();
        if (req == null) return;
        resident.updateRequest(req.getRequestId());
        if (!"Pending".equalsIgnoreCase(req.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending requests can be updated.",
                    "Update Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        RequestFormFields fields = new RequestFormFields();
        fields.detailsField.setText(req.getDetails());
        if (req instanceof MedicalRequest medical) {
            fields.itemField.setText(medical.getMedicalItem());
            fields.quantityField.setText(medical.getQuantity());
        } else if (req instanceof FoodRequest food) {
            fields.itemField.setText(food.getFoodItems());
            fields.quantityField.setText(food.getQuantity());
        } else if (req instanceof TransportationRequest transportation) {
            fields.pickupField.setText(transportation.getPickupLocation());
            fields.destinationField.setText(transportation.getDestination());
            fields.whenNeededField.setText(transportation.getWhenNeeded().format(WHEN_NEEDED_FORMAT));
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        addFormRow(panel, "Type:", new JLabel(req.getType()));
        addFormRow(panel, "Details:", fields.detailsField);
        addTypeFields(panel, req.getType(), fields);
        JScrollPane formScroll = new JScrollPane(panel);
        formScroll.setPreferredSize(new Dimension(520, 230));
        Object[] options = {"Save Changes", "Cancel Request", "Close"};
        JOptionPane optionPane = new JOptionPane(formScroll, JOptionPane.PLAIN_MESSAGE,
            JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        JDialog dialog = optionPane.createDialog(this, "Update Request");
        dialog.setResizable(true);
        dialog.setVisible(true);
        Object choice = optionPane.getValue();
        dialog.dispose();

        if (options[1].equals(choice)) {
            resident.cancelRequest();
            cancelRequest(req);
            return;
        }
        if (!options[0].equals(choice) || !hasRequiredFields(req.getType(), fields)) return;

        req.setDetails(fields.detailsField.getText().trim());
        if (req instanceof MedicalRequest medical) {
            medical.setMedicalItem(fields.itemField.getText().trim());
            medical.setQuantity(fields.quantityField.getText().trim());
        } else if (req instanceof FoodRequest food) {
            food.setFoodItems(fields.itemField.getText().trim());
            food.setQuantity(fields.quantityField.getText().trim());
        } else if (req instanceof TransportationRequest transportation) {
            transportation.setPickupLocation(fields.pickupField.getText().trim());
            transportation.setDestination(fields.destinationField.getText().trim());
            transportation.setWhenNeeded(parseWhenNeeded(fields.whenNeededField.getText().trim()));
        }
        requestManager.updateRequest(req.getRequestId());
        fileManager.recordResidentRequestAction(resident, req, "Updated");
        JOptionPane.showMessageDialog(this, "Request updated.");
        refreshTable();
    }

    private void addTypeFields(JPanel panel, String type, RequestFormFields fields) {
        if ("Medical".equals(type)) {
            addFormRow(panel, "Medicine / Medical item:", fields.itemField);
            addFormRow(panel, "Quantity:", fields.quantityField);
        } else if ("Food".equals(type)) {
            addFormRow(panel, "Food items:", fields.itemField);
            addFormRow(panel, "Quantity:", fields.quantityField);
        } else if ("Transportation".equals(type)) {
            addFormRow(panel, "Pickup location:", fields.pickupField);
            addFormRow(panel, "Destination:", fields.destinationField);
            addFormRow(panel, "Needed at (yyyy-MM-dd HH:mm):", fields.whenNeededField);
        }
    }

    private void addFormRow(JPanel panel, String label, Component input) {
        panel.add(new JLabel(label));
        panel.add(input);
    }

    private boolean hasRequiredFields(String type, RequestFormFields fields) {
        if (fields.detailsField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter request details.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (("Medical".equals(type) || "Food".equals(type))
                && (fields.itemField.getText().trim().isEmpty()
                || fields.quantityField.getText().trim().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Please enter the requested item and quantity.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ("Transportation".equals(type)
                && (fields.pickupField.getText().trim().isEmpty()
                || fields.destinationField.getText().trim().isEmpty()
                || fields.whenNeededField.getText().trim().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Please enter pickup, destination, and needed date/time.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ("Transportation".equals(type)) {
            try {
                parseWhenNeeded(fields.whenNeededField.getText().trim());
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Enter needed date/time as yyyy-MM-dd HH:mm.",
                        "Invalid Date/Time", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private LocalDateTime parseWhenNeeded(String value) {
        return LocalDateTime.parse(value, WHEN_NEEDED_FORMAT);
    }

    private static class RequestFormFields {
        private final JTextField detailsField = new JTextField();
        private final JTextField itemField = new JTextField();
        private final JTextField quantityField = new JTextField();
        private final JTextField pickupField = new JTextField();
        private final JTextField destinationField = new JTextField();
        private final JTextField whenNeededField = new JTextField(
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).format(WHEN_NEEDED_FORMAT));
    }

    private void cancelRequest(AssistanceRequest req) {
        int confirm = JOptionPane.showConfirmDialog(this, "Cancel request " + req.getRequestId() + "?",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            req.updateStatus("Cancelled");
            requestManager.updateRequest(req.getRequestId());
            fileManager.recordResidentRequestAction(resident, req, "Cancelled");
            JOptionPane.showMessageDialog(this, "Request marked as cancelled.");
            refreshTable();
        }
    }
}
