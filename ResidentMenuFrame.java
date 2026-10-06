import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
            tableModel.addRow(new Object[]{r.getRequestId(), r.getType(), r.toDisplaySummary(), r.getStatus()});
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
        String[] types = {"Food", "Medical", "Transportation"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        RequestFormFields fields = new RequestFormFields();
        JPanel panel = new JPanel();
        Runnable rebuildForm = () -> rebuildRequestForm(panel,
                (String) typeBox.getSelectedItem(), fields, typeBox);
        typeBox.addActionListener(e -> rebuildForm.run());
        rebuildForm.run();

        JScrollPane formScroll = new JScrollPane(panel);
        formScroll.setPreferredSize(new Dimension(560, 440));
        JDialog dialog = new JDialog(this, "Submit Assistance Request", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.add(formScroll, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");
        JButtonStyle.applyAccent(submitButton);
        JButtonStyle.applySecondary(cancelButton);
        buttons.add(submitButton);
        buttons.add(cancelButton);
        dialog.add(buttons, BorderLayout.SOUTH);
        submitButton.addActionListener(e -> {
            String type = toRequestType((String) typeBox.getSelectedItem());
            if (!hasRequiredFields(type, fields)) return;
            AssistanceRequest newRequest = createRequest(type, requestManager.nextRequestId(), fields,
                    "Pending", LocalDateTime.now());
            try {
                requestManager.addRequest(newRequest);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Could not save the request: " + ex.getMessage(),
                        "Save Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(dialog, "Request submitted successfully!\n"
                    + "Your Reference No. is: " + newRequest.getRequestId() + "\n"
                    + "Please keep this number for tracking your request.",
                    "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
            fields.reset();
            typeBox.setSelectedIndex(0);
            rebuildForm.run();
            refreshTable();
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.pack();
        dialog.setMinimumSize(new Dimension(590, 520));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
        fields.setFromRequest(req);
        if (req instanceof MedicalRequest medical) {
            fields.setMedicalItems(medical.getMedicalItems());
        } else if (req instanceof FoodRequest food) {
            fields.setFoodItems(food.getFoodItems());
        } else if (req instanceof TransportationRequest transportation) {
            fields.pickupField.setText(transportation.getPickupLocation());
            fields.destinationField.setText(transportation.getDestination());
            fields.passengerCountField.setText(String.valueOf(transportation.getPassengerCount()));
        }

        JPanel panel = new JPanel();
        rebuildRequestForm(panel, req.getType(), fields, new JLabel(toAssistanceLabel(req.getType())));
        JScrollPane formScroll = new JScrollPane(panel);
        formScroll.setPreferredSize(new Dimension(560, 420));
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

        req.setLocation(fields.locationField.getText().trim());
        req.setTimeNeeded(parseWhenNeeded(fields.whenNeededField.getText().trim()));
        req.setAdditionalDetails(fields.additionalDetailsArea.getText().trim());
        if (req instanceof MedicalRequest medical) {
            medical.setMedicalItems(fields.readMedicalItems());
        } else if (req instanceof FoodRequest food) {
            food.setFoodItems(fields.readFoodItems());
        } else if (req instanceof TransportationRequest transportation) {
            transportation.setPickupLocation(fields.pickupField.getText().trim());
            transportation.setDestination(fields.destinationField.getText().trim());
            transportation.setPassengerCount(Integer.parseInt(fields.passengerCountField.getText().trim()));
        }
        requestManager.updateRequest(req.getRequestId());
        fileManager.recordResidentRequestAction(resident, req, "Updated");
        JOptionPane.showMessageDialog(this, "Request updated.");
        refreshTable();
    }

    private void rebuildRequestForm(JPanel panel, String selectedType, RequestFormFields fields,
                                    Component typeInput) {
        String type = toRequestType(selectedType);
        panel.removeAll();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        addFormRow(panel, "Assistance Type:", typeInput);
        JLabel heading = new JLabel(toAssistanceLabel(type).toUpperCase());
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 15f));
        heading.setForeground(new Color(27, 42, 67));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, heading.getPreferredSize().height));
        panel.add(Box.createVerticalStrut(8));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(8));

        if ("Food".equals(type) || "Medical".equals(type)) {
            String itemLabel = "Food".equals(type) ? "Food Item" : "Medical Item";
            String[] units = "Food".equals(type)
                    ? new String[]{"kg", "pcs", "pack", "g", "liter", "ml"}
                    : new String[]{"pcs", "box", "bottle", "pack", "tablet", "ml"};
            for (ItemFormRow row : fields.itemRows) row.setUnits(units);
            JButton addItemButton = new JButton("+");
            addItemButton.setToolTipText("Add another " + itemLabel.toLowerCase());
            JButtonStyle.applySecondary(addItemButton);
            JPanel addItemPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
            addItemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            addItemPanel.add(addItemButton);
            addItemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, addItemPanel.getPreferredSize().height));
            panel.add(addItemPanel);

            JPanel itemTable = new JPanel(new GridLayout(0, 3, 8, 5));
            itemTable.setAlignmentX(Component.LEFT_ALIGNMENT);
            itemTable.add(new JLabel(itemLabel));
            itemTable.add(new JLabel("No."));
            itemTable.add(new JLabel("Unit"));
            for (ItemFormRow row : fields.itemRows) {
                itemTable.add(row.nameField);
                itemTable.add(row.quantityField);
                itemTable.add(row.unitBox);
            }
            itemTable.setMaximumSize(new Dimension(Integer.MAX_VALUE, itemTable.getPreferredSize().height));
            panel.add(itemTable);
            addItemButton.addActionListener(e -> {
                fields.itemRows.add(new ItemFormRow(units));
                rebuildRequestForm(panel, selectedType, fields, typeInput);
            });
        } else {
            addFormRow(panel, "Pickup Location:", fields.pickupField);
            addFormRow(panel, "Destination:", fields.destinationField);
            addFormRow(panel, "No. of Passengers:", fields.passengerCountField);
        }
        if (!"Transportation".equals(type)) {
            addFormRow(panel, "Location:", fields.locationField);
        }
        addFormRow(panel, "Time Needed (yyyy-MM-dd HH:mm):", fields.whenNeededField);
        addFormRow(panel, "Additional Details:", new JScrollPane(fields.additionalDetailsArea));
        panel.revalidate();
        panel.repaint();
    }

    private void addFormRow(JPanel panel, String label, Component input) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(38, input.getPreferredSize().height + 4)));
        JLabel labelComponent = new JLabel(label);
        labelComponent.setPreferredSize(new Dimension(175, 24));
        row.add(labelComponent, BorderLayout.WEST);
        row.add(input, BorderLayout.CENTER);
        panel.add(Box.createVerticalStrut(3));
        panel.add(row);
    }

    private boolean hasRequiredFields(String type, RequestFormFields fields) {
        if (fields.locationField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the assistance location.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (fields.whenNeededField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter when the assistance is needed.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            parseWhenNeeded(fields.whenNeededField.getText().trim());
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter needed date/time as yyyy-MM-dd HH:mm.",
                    "Invalid Date/Time", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ("Transportation".equals(type)) {
            if (fields.pickupField.getText().trim().isEmpty()
                    || fields.destinationField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both pickup location and destination.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return validatePositiveInteger(fields.passengerCountField, "number of passengers");
        }
        return validateItemRows(fields);
    }

    private boolean validateItemRows(RequestFormFields fields) {
        boolean hasItem = false;
        for (ItemFormRow row : fields.itemRows) {
            String name = row.nameField.getText().trim();
            String quantity = row.quantityField.getText().trim();
            boolean empty = name.isEmpty() && quantity.isEmpty();
            if (empty) continue;
            if (name.isEmpty() || quantity.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete each item row or leave it entirely blank.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (!isPositiveInteger(quantity)) {
                JOptionPane.showMessageDialog(this, "Item quantities must be whole numbers greater than zero.",
                        "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            hasItem = true;
        }
        if (!hasItem) {
            JOptionPane.showMessageDialog(this, "Please add at least one requested item.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
        }
        return hasItem;
    }

    private boolean validatePositiveInteger(JTextField field, String label) {
        if (!isPositiveInteger(field.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Enter a whole number greater than zero for " + label + ".",
                    "Invalid Number", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private AssistanceRequest createRequest(String type, String requestId, RequestFormFields fields,
                                             String status, LocalDateTime dateRequested) {
        LocalDateTime timeNeeded = parseWhenNeeded(fields.whenNeededField.getText().trim());
        String additionalDetails = fields.additionalDetailsArea.getText().trim();
        if ("Food".equals(type)) {
            return new FoodRequest(requestId, resident.getResidentId(), fields.locationField.getText().trim(),
                    timeNeeded, additionalDetails, status, dateRequested, fields.readFoodItems());
        }
        if ("Medical".equals(type)) {
            return new MedicalRequest(requestId, resident.getResidentId(), fields.locationField.getText().trim(),
                    timeNeeded, additionalDetails, status, dateRequested, fields.readMedicalItems());
        }
        String pickup = fields.pickupField.getText().trim();
        return new TransportationRequest(requestId, resident.getResidentId(), pickup,
                fields.destinationField.getText().trim(),
                Integer.parseInt(fields.passengerCountField.getText().trim()), timeNeeded,
                additionalDetails, status, dateRequested);
    }

    private String toRequestType(String label) {
        if (label == null) return "Food";
        if (label.startsWith("Food")) return "Food";
        if (label.startsWith("Medical")) return "Medical";
        return "Transportation";
    }

    private String toAssistanceLabel(String type) {
        return type + " Assistance";
    }

    private LocalDateTime parseWhenNeeded(String value) {
        return LocalDateTime.parse(value, WHEN_NEEDED_FORMAT);
    }

    private static class RequestFormFields {
        private final List<ItemFormRow> itemRows = new ArrayList<>();
        private final JTextField locationField = new JTextField();
        private final JTextField pickupField = new JTextField();
        private final JTextField destinationField = new JTextField();
        private final JTextField passengerCountField = new JTextField("1");
        private final JTextField whenNeededField = new JTextField(
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).format(WHEN_NEEDED_FORMAT));
        private final JTextArea additionalDetailsArea = new JTextArea(3, 24);

        private RequestFormFields() {
            reset();
        }

        private void reset() {
            locationField.setText("");
            pickupField.setText("");
            destinationField.setText("");
            passengerCountField.setText("1");
            whenNeededField.setText(LocalDateTime.now().plusDays(1)
                    .withHour(9).withMinute(0).format(WHEN_NEEDED_FORMAT));
            additionalDetailsArea.setText("");
            itemRows.clear();
            for (int i = 0; i < 3; i++) {
                itemRows.add(new ItemFormRow(new String[]{"kg", "pcs", "pack", "g", "liter", "ml"}));
            }
        }

        private void setFromRequest(AssistanceRequest request) {
            locationField.setText(request.getLocation());
            whenNeededField.setText(request.getTimeNeeded().format(WHEN_NEEDED_FORMAT));
            additionalDetailsArea.setText(request.getAdditionalDetails());
        }

        private void setFoodItems(List<FoodItem> items) {
            itemRows.clear();
            for (FoodItem item : items) {
                ItemFormRow row = new ItemFormRow(new String[]{"kg", "pcs", "pack", "g", "liter", "ml"});
                row.nameField.setText(item.getName());
                row.quantityField.setText(String.valueOf(item.getQuantity()));
                row.setSelectedUnit(item.getUnit());
                itemRows.add(row);
            }
            if (itemRows.isEmpty()) itemRows.add(new ItemFormRow(new String[]{"kg", "pcs", "pack"}));
        }

        private void setMedicalItems(List<MedicalItem> items) {
            itemRows.clear();
            for (MedicalItem item : items) {
                ItemFormRow row = new ItemFormRow(new String[]{"pcs", "box", "bottle", "pack", "tablet", "ml"});
                row.nameField.setText(item.getName());
                row.quantityField.setText(String.valueOf(item.getQuantity()));
                row.setSelectedUnit(item.getUnit());
                itemRows.add(row);
            }
            if (itemRows.isEmpty()) itemRows.add(new ItemFormRow(new String[]{"pcs", "box", "bottle"}));
        }

        private List<FoodItem> readFoodItems() {
            List<FoodItem> items = new ArrayList<>();
            for (ItemFormRow row : itemRows) {
                String name = row.nameField.getText().trim();
                if (!name.isEmpty()) {
                    items.add(new FoodItem(name, Integer.parseInt(row.quantityField.getText().trim()),
                            (String) row.unitBox.getSelectedItem()));
                }
            }
            return items;
        }

        private List<MedicalItem> readMedicalItems() {
            List<MedicalItem> items = new ArrayList<>();
            for (ItemFormRow row : itemRows) {
                String name = row.nameField.getText().trim();
                if (!name.isEmpty()) {
                    items.add(new MedicalItem(name, Integer.parseInt(row.quantityField.getText().trim()),
                            (String) row.unitBox.getSelectedItem()));
                }
            }
            return items;
        }
    }

    private static class ItemFormRow {
        private final JTextField nameField = new JTextField();
        private final JTextField quantityField = new JTextField();
        private final JComboBox<String> unitBox;

        private ItemFormRow(String[] units) {
            unitBox = new JComboBox<>(units);
        }

        private void setUnits(String[] units) {
            String selected = (String) unitBox.getSelectedItem();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(units);
            boolean containsSelected = false;
            for (String unit : units) {
                if (unit.equals(selected)) {
                    containsSelected = true;
                    break;
                }
            }
            if (selected != null && !containsSelected) model.addElement(selected);
            unitBox.setModel(model);
            if (selected != null) unitBox.setSelectedItem(selected);
        }

        private void setSelectedUnit(String unit) {
            unitBox.setSelectedItem(unit);
            if (!unit.equals(unitBox.getSelectedItem())) {
                unitBox.addItem(unit);
                unitBox.setSelectedItem(unit);
            }
        }
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
