import java.time.LocalDateTime;

/** Transportation assistance request for a pickup, destination, and passenger count. */
public class TransportationRequest extends AssistanceRequest {
    private String pickupLocation;
    private String destination;
    private int passengerCount;

    public TransportationRequest(String requestId, String residentId, String pickupLocation, String destination,
                                 int passengerCount, LocalDateTime timeNeeded, String additionalDetails,
                                 String status, LocalDateTime dateRequested) {
        super(requestId, residentId, "Transportation", pickupLocation, timeNeeded,
                additionalDetails, status, dateRequested);
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.passengerCount = passengerCount;
    }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
        setLocation(pickupLocation);
    }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }

    @Override
    public void displayDetails() {
        System.out.println(toDisplaySummary());
    }

    @Override
    public String toDisplaySummary() {
        return "[Transportation] " + getRequestId() + " - " + getStatus() + " - "
                + pickupLocation + " -> " + destination + " - Passengers: " + passengerCount
                + " - Needed: " + getTimeNeeded().format(DT_FMT) + " - " + getAdditionalDetails();
    }

    @Override
    public String toFileLine() {
        return commonFieldsForFile("TRANSPORT") + "|" + encodeField(pickupLocation) + "|"
                + encodeField(destination) + "|" + passengerCount;
    }
}
