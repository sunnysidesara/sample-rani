import java.time.LocalDateTime;

/** TransportationRequest: request for barangay transport assistance. */
public class TransportationRequest extends AssistanceRequest {
    private String pickupLocation;
    private String destination;
    private LocalDateTime whenNeeded;

    public TransportationRequest(String requestId, String residentId, String details, String status,
                                  LocalDateTime dateRequested, String pickupLocation, String destination,
                                  LocalDateTime whenNeeded) {
        super(requestId, residentId, "Transportation", details, status, dateRequested);
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.whenNeeded = whenNeeded;
    }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDateTime getWhenNeeded() { return whenNeeded; }
    public void setWhenNeeded(LocalDateTime whenNeeded) { this.whenNeeded = whenNeeded; }

    @Override
    public void displayDetails() {
        System.out.println(toDisplaySummary());
    }

    @Override
    public String toDisplaySummary() {
        return "[Transportation] " + getRequestId() + " - " + getStatus() + " - " + pickupLocation
                + " -> " + destination + " @ " + whenNeeded.format(DT_FMT) + " - " + getDetails();
    }

    @Override
    public String toFileLine() {
        return String.join("|", "TRANSPORT", getRequestId(), getResidentId(), getType(), getDetails(),
                getStatus(), getDateRequested().format(DT_FMT), pickupLocation, destination,
                whenNeeded.format(DT_FMT));
    }
}
