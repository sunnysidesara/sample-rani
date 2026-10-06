import java.util.List;

/**
 * Admin: manages user accounts and has oversight of all requests.
 */
public class Admin extends User {
    private String adminId;

    public Admin(String name, String password, String adminId) {
        super(name, password, "Admin");
        this.adminId = adminId;
    }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public void createUser(User user) {
        if (user == null) throw new IllegalArgumentException("User is required.");
        new FileManager().saveUser(user);
    }

    public void manageUser(String userId) {
        System.out.println(getFullName() + " is managing user " + userId + ".");
    }

    public void deleteUser(String userId) {
        FileManager fileManager = new FileManager();
        List<User> users = fileManager.readUsers();
        users.removeIf(user -> getUserId(user).equals(userId));
        fileManager.saveAllUsers(users);
    }

    private String getUserId(User user) {
        if (user instanceof Resident resident) return resident.getResidentId();
        if (user instanceof Admin admin) return admin.getAdminId();
        if (user instanceof Staff staff) return staff.getStaffId();
        return "";
    }

    public void viewAllRequest() { System.out.println(getFullName() + " is viewing all requests."); }
    public void viewUserHistory() { System.out.println(getFullName() + " is viewing a user's history."); }

    @Override
    public void showMenu() {
        new AdminMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "Admin", getFullName(), getPassword(), adminId);
    }
}
