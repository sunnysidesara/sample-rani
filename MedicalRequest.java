import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Medical assistance request containing one or more medical items. */
public class MedicalRequest extends AssistanceRequest {
    private final List<MedicalItem> medicalItems;

    public MedicalRequest(String requestId, String residentId, String location, LocalDateTime timeNeeded,
                          String additionalDetails, String status, LocalDateTime dateRequested,
                          List<MedicalItem> medicalItems) {
        super(requestId, residentId, "Medical", location, timeNeeded, additionalDetails, status, dateRequested);
        this.medicalItems = new ArrayList<>(medicalItems);
    }

    public List<MedicalItem> getMedicalItems() {
        return Collections.unmodifiableList(medicalItems);
    }

    public void setMedicalItems(List<MedicalItem> items) {
        List<MedicalItem> copy = new ArrayList<>(items);
        medicalItems.clear();
        medicalItems.addAll(copy);
    }

    @Override
    public void displayDetails() {
        System.out.println(toDisplaySummary());
    }

    @Override
    public String toDisplaySummary() {
        return "[Medical] " + getRequestId() + " - " + getStatus() + " - " + medicalItems
                + " - Location: " + getLocation() + " - Needed: " + getTimeNeeded().format(DT_FMT)
                + " - " + getAdditionalDetails();
    }

    @Override
    public String toFileLine() {
        StringBuilder line = new StringBuilder(commonFieldsForFile("MEDICAL"));
        line.append('|').append(medicalItems.size());
        for (MedicalItem item : medicalItems) {
            line.append('|').append(encodeField(item.getName()))
                    .append('|').append(item.getQuantity())
                    .append('|').append(encodeField(item.getUnit()));
        }
        return line.toString();
    }
}
