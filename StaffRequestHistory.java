import java.time.LocalDateTime;

/** Persistent record of a staff member processing or deleting a request. */
public class StaffRequestHistory {
    private String staffId;
    private final String requestId;
    private final String residentId;
    private final String requestType;
    private final String status;
    private final LocalDateTime processedAt;

    public StaffRequestHistory(String staffId, String requestId, String residentId, String requestType,
                               String status, LocalDateTime processedAt) {
        this.staffId = staffId;
        this.requestId = requestId;
        this.residentId = residentId;
        this.requestType = requestType;
        this.status = status;
        this.processedAt = processedAt;
    }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    public String getRequestId() { return requestId; }
    public String getResidentId() { return residentId; }
    public String getRequestType() { return requestType; }
    public String getStatus() { return status; }
    public LocalDateTime getProcessedAt() { return processedAt; }

    public String toFileLine() {
        return String.join("|", staffId, requestId, residentId, requestType, status, processedAt.toString());
    }
}
