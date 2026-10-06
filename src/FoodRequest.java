import java.time.LocalDateTime;

/** FoodRequest: request for food packs/relief goods. */
public class FoodRequest extends AssistanceRequest {
    private String foodItems;
    private String quantity;

    public FoodRequest(String requestId, String residentId, String details, String status,
                        LocalDateTime dateRequested, String foodItems, String quantity) {
        super(requestId, residentId, "Food", details, status, dateRequested);
        this.foodItems = foodItems;
        this.quantity = quantity;
    }

    public String getFoodItems() { return foodItems; }
    public void setFoodItems(String foodItems) { this.foodItems = foodItems; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    @Override
    public void displayDetails() {
        System.out.println(toDisplaySummary());
    }

    @Override
    public String toDisplaySummary() {
        return "[Food] " + getRequestId() + " - " + getStatus() + " - Items: " + foodItems
                + " x" + quantity + " - " + getDetails();
    }

    @Override
    public String toFileLine() {
        return String.join("|", "FOOD", getRequestId(), getResidentId(), getType(), getDetails(),
                getStatus(), getDateRequested().format(DT_FMT), foodItems, quantity);
    }
}
