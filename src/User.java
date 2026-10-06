import java.io.Serializable;

/**
 * Abstract base class for every account type in the system.
 */
public abstract class User implements Serializable {
    private String password;
    private String fullName;
    private String role;
    private boolean credentialsVerified;

    public User(String fullName, String password, String role) {
        this.fullName = fullName;
        this.password = password;
        this.role = role;
    }

    // Encapsulation: getters/setters
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean authenticate(String attemptPassword) {
        credentialsVerified = password != null && password.equals(attemptPassword);
        return credentialsVerified;
    }

    /** Completes login after the calling interface has verified the password. */
    public boolean login() {
        boolean successfulLogin = credentialsVerified;
        credentialsVerified = false;
        return successfulLogin;
    }

    public void logout() {
        System.out.println(fullName + " has logged out.");
    }

    /** Each role renders its own menu window (polymorphism). */
    public abstract void showMenu();

    /** Converts this user to a pipe-delimited line for file storage. Subclasses append their own fields. */
    public abstract String toFileLine();
}
