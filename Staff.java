/**
 * Staff: base class for barangay personnel who process assistance requests
 * of a specific assigned type (Medical, Food, Transportation).
 */
public class Staff extends User {
    private String staffId;
    private String assignedType;

    public Staff(String name, String password, String staffId, String assignedType) {
        this(name, password, "Staff", staffId, assignedType);
    }

    // Protected-style constructor used by subclasses to set their own role label.
    protected Staff(String name, String password, String role, String staffId, String assignedType) {
        super(name, password, role);
        this.staffId = staffId;
        this.assignedType = assignedType;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    @Override public String getUserId() { return staffId; }
    public String getAssignedType() { return assignedType; }
    public void setAssignedType(String assignedType) { this.assignedType = assignedType; }

    // Conceptual entry points; full logic lives in StaffMenuFrame + FileManager.
    public void viewRequests() { System.out.println(getFullName() + " is viewing requests."); }
    public void searchRequest() { System.out.println(getFullName() + " is searching a request."); }
    public void updateRequestStatus() { System.out.println(getFullName() + " is updating a request status."); }
    public void deleteRequest() { System.out.println(getFullName() + " is deleting a request."); }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "Staff", getFullName(), getPassword(), staffId, assignedType);
    }
}
