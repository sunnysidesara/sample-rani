/**
 * Resident: a barangay constituent who can submit and manage assistance requests.
 */
public class Resident extends User {
    private String residentId;
    private String address;
    private String contactNo;

    public Resident(String fullName, String password, String residentId, String address, String contactNo) {
        super(fullName, password, "Resident");
        this.residentId = residentId;
        this.address = address;
        this.contactNo = contactNo;
    }

    public String getResidentId() { return residentId; }
    public void setResidentId(String residentId) { this.residentId = residentId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public void submitRequest(AssistanceRequest request) {
        if (request == null || !residentId.equals(request.getResidentId())) {
            throw new IllegalArgumentException("Request must belong to this resident.");
        }
        new RequestManager(new FileManager()).addRequest(request);
    }

    public void viewMyRequest() { System.out.println(getFullName() + " is viewing their requests."); }
    public void updateRequest(String requestId) {
        System.out.println(getFullName() + " is updating request " + requestId + ".");
    }
    public void cancelRequest() { System.out.println(getFullName() + " is cancelling a request."); }

    @Override
    public void showMenu() {
        new ResidentMenuFrame(this).setVisible(true);
    }

    @Override
    public String toFileLine() {
        return String.join("|", "Resident", getFullName(), getPassword(), residentId, address, contactNo);
    }
}
