import java.io.Serializable;

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

    public abstract String getUserId();

    public abstract String toFileLine();
}
