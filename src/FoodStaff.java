/** FoodStaff: handles Food-type assistance requests. Overrides showMenu(). */
public class FoodStaff extends Staff {
    public FoodStaff(String name, String password, String staffId) {
        super(name, password, "FoodStaff", staffId, "Food");
    }

    @Override
    public void showMenu() {
        new StaffMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "FoodStaff", getFullName(), getPassword(), getStaffId(), getAssignedType());
    }
}
