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
    private String details;
    private String status;
    private LocalDateTime dateRequested;

    public AssistanceRequest(String requestId, String residentId, String type, String details,
                              String status, LocalDateTime dateRequested) {
        this.requestId = requestId;
        this.residentId = residentId;
        this.type = type;
        this.details = details;
        this.status = status;
        this.dateRequested = dateRequested;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getResidentId() { return residentId; }
    public void setResidentId(String residentId) { this.residentId = residentId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
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
        return "[" + type + "] " + requestId + " - " + status + " - " + details;
    }

    /** Serializes the common fields to a pipe-delimited line; subclasses append their own fields. */
    public abstract String toFileLine();
}
