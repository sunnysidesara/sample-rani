/** TransportationStaff: handles Transportation-type assistance requests. Overrides showMenu(). */
public class TransportationStaff extends Staff {
    public TransportationStaff(String name, String password, String staffId) {
        super(name, password, "TransportationStaff", staffId, "Transportation");
    }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "TransportationStaff", getFullName(), getPassword(), getStaffId(), getAssignedType());
    }
}
