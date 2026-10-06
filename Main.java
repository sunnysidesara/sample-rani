import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Entry point: initializes the FileManager, seeds dummy data on first run,
 * and launches the login GUI window.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        FileManager fileManager = new FileManager();
        seedDummyDataIfEmpty(fileManager);

        SwingUtilities.invokeLater(() -> new LoginFrame(fileManager).setVisible(true));
    }

    /** Loads a handful of sample accounts/requests so the app is usable on a fresh checkout. */
    private static void seedDummyDataIfEmpty(FileManager fm) {
        List<User> users = fm.readUsers();
        if (users.isEmpty()) {
            fm.saveUser(new Admin("admin", "admin123", "ADM1000"));
            fm.saveUser(new MedicalStaff("Dr. Santos", "med123", "STF2000"));
            fm.saveUser(new FoodStaff("Aling Nena", "food123", "STF2001"));
            fm.saveUser(new TransportationStaff("Mang Tomas", "trans123", "STF2002"));
            fm.saveUser(new Resident("Juan Dela Cruz", "res123", "RSD3000", "123 Rizal St.", "917123456"));
        }

        List<AssistanceRequest> requests = fm.readRequests();
        if (requests.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            fm.saveRequest(new MedicalRequest("REQ4000", "RSD3000", "123 Rizal St.",
                    now.plusDays(1).withHour(9).withMinute(0), "Needed for hypertension", "Pending", now,
                    Arrays.asList(new MedicalItem("Amlodipine", 1, "box"))));
        }
    }
}
