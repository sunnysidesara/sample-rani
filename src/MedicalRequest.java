import java.time.LocalDateTime;

/** MedicalRequest: request for medicine/medical supplies. */
public class MedicalRequest extends AssistanceRequest {
    private String medicalItem;
    private String quantity;

    public MedicalRequest(String requestId, String residentId, String details, String status,
                           LocalDateTime dateRequested, String medicalItem, String quantity) {
        super(requestId, residentId, "Medical", details, status, dateRequested);
        this.medicalItem = medicalItem;
        this.quantity = quantity;
    }

    public String getMedicalItem() { return medicalItem; }
    public void setMedicalItem(String medicalItem) { this.medicalItem = medicalItem; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    @Override
    public void displayDetails() {
        System.out.println(toDisplaySummary());
    }

    @Override
    public String toDisplaySummary() {
        return "[Medical] " + getRequestId() + " - " + getStatus() + " - Item: " + medicalItem
                + " x" + quantity + " - " + getDetails();
    }

    @Override
    public String toFileLine() {
        return String.join("|", "MEDICAL", getRequestId(), getResidentId(), getType(), getDetails(),
                getStatus(), getDateRequested().format(DT_FMT), medicalItem, quantity);
    }
}
