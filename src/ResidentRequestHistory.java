import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/** Persistent snapshot of a resident editing or cancelling a request. */
public class ResidentRequestHistory {
    private String residentId;
    private final String requestId;
    private final String requestType;
    private final String action;
    private final String requestSummary;
    private final LocalDateTime updatedAt;

    public ResidentRequestHistory(String residentId, String requestId, String requestType, String action,
                                  String requestSummary, LocalDateTime updatedAt) {
        this.residentId = residentId;
        this.requestId = requestId;
        this.requestType = requestType;
        this.action = action;
        this.requestSummary = requestSummary;
        this.updatedAt = updatedAt;
    }

    public String getResidentId() { return residentId; }
    public void setResidentId(String residentId) { this.residentId = residentId; }
    public String getRequestId() { return requestId; }
    public String getRequestType() { return requestType; }
    public String getAction() { return action; }
    public String getRequestSummary() { return requestSummary; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String toFileLine() {
        String encodedSummary = Base64.getEncoder().encodeToString(requestSummary.getBytes(StandardCharsets.UTF_8));
        return String.join("|", residentId, requestId, requestType, action, encodedSummary,
                updatedAt.format(AssistanceRequest.DT_FMT));
    }

    public static ResidentRequestHistory fromFileLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 6) return null;
        try {
            String summary = new String(Base64.getDecoder().decode(fields[4]), StandardCharsets.UTF_8);
            LocalDateTime updatedAt = LocalDateTime.parse(fields[5], AssistanceRequest.DT_FMT);
            return new ResidentRequestHistory(fields[0], fields[1], fields[2], fields[3], summary, updatedAt);
        } catch (Exception e) {
            return null;
        }
    }
}