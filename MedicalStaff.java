/** MedicalStaff: handles Medical-type assistance requests. Overrides showMenu(). */
public class MedicalStaff extends Staff {
    public MedicalStaff(String name, String password, String staffId) {
        super(name, password, "MedicalStaff", staffId, "Medical");
    }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "MedicalStaff", getFullName(), getPassword(), getStaffId(), getAssignedType());
    }
}
