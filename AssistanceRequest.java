import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for all assistance requests filed by residents.
 */
public abstract class AssistanceRequest implements Serializable {
    public static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private String requestId;
    private String residentId;
    private String type;
    private String location;
    private LocalDateTime timeNeeded;
    private String additionalDetails;
    private String status;
    private LocalDateTime dateRequested;

    public AssistanceRequest(String requestId, String residentId, String type, String location,
                             LocalDateTime timeNeeded, String additionalDetails, String status,
                             LocalDateTime dateRequested) {
        this.requestId = requestId;
        this.residentId = residentId;
        this.type = type;
        this.location = location;
        this.timeNeeded = timeNeeded;
        this.additionalDetails = additionalDetails;
        this.status = status;
        this.dateRequested = dateRequested;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getResidentId() { return residentId; }
    public void setResidentId(String residentId) { this.residentId = residentId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getTimeNeeded() { return timeNeeded; }
    public void setTimeNeeded(LocalDateTime timeNeeded) { this.timeNeeded = timeNeeded; }
    public String getAdditionalDetails() { return additionalDetails; }
    public void setAdditionalDetails(String additionalDetails) { this.additionalDetails = additionalDetails; }
    public String getDetails() { return getAdditionalDetails(); }
    public void setDetails(String details) { setAdditionalDetails(details); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDateRequested() { return dateRequested; }
    public void setDateRequested(LocalDateTime dateRequested) { this.dateRequested = dateRequested; }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }

    /** Prints the request's details to the console. Subclasses override to add their own fields. */
    public void displayDetails() {
        System.out.println(toDisplaySummary());
    }

    /** Human-readable summary used by GUI tables/dialogs. Subclasses override (polymorphism). */
    public String toDisplaySummary() {
        return "[" + type + "] " + requestId + " - " + status + " - " + additionalDetails;
    }

    protected String commonFieldsForFile(String recordType) {
        return String.join("|", "V2", encodeField(recordType), encodeField(requestId),
                encodeField(residentId), encodeField(type), encodeField(location),
                timeNeeded.format(DT_FMT), encodeField(additionalDetails), encodeField(status),
                dateRequested.format(DT_FMT));
    }

    static String encodeField(String value) {
        return value.replace("%", "%25").replace("|", "%7C")
                .replace("\r", "%0D").replace("\n", "%0A");
    }

    static String decodeField(String value) {
        return value.replace("%7C", "|").replace("%0D", "\r")
                .replace("%0A", "\n").replace("%25", "%");
    }

    public abstract String toFileLine();
}
