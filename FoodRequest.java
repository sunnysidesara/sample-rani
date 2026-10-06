import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Food assistance request containing one or more food items. */
public class FoodRequest extends AssistanceRequest {
    private final List<FoodItem> foodItems;

    public FoodRequest(String requestId, String residentId, String location, LocalDateTime timeNeeded,
                       String additionalDetails, String status, LocalDateTime dateRequested,
                       List<FoodItem> foodItems) {
        super(requestId, residentId, "Food", location, timeNeeded, additionalDetails, status, dateRequested);
        this.foodItems = new ArrayList<>(foodItems);
    }

    public List<FoodItem> getFoodItems() {
        return Collections.unmodifiableList(foodItems);
    }

    public void setFoodItems(List<FoodItem> items) {
        List<FoodItem> copy = new ArrayList<>(items);
        foodItems.clear();
        foodItems.addAll(copy);
    }

    @Override
    public void displayDetails() {
        System.out.println(toDisplaySummary());
    }

    @Override
    public String toDisplaySummary() {
        return "[Food] " + getRequestId() + " - " + getStatus() + " - " + foodItems
                + " - Location: " + getLocation() + " - Needed: " + getTimeNeeded().format(DT_FMT)
                + " - " + getAdditionalDetails();
    }

    @Override
    public String toFileLine() {
        StringBuilder line = new StringBuilder(commonFieldsForFile("FOOD"));
        line.append('|').append(foodItems.size());
        for (FoodItem item : foodItems) {
            line.append('|').append(encodeField(item.getName()))
                    .append('|').append(item.getQuantity())
                    .append('|').append(encodeField(item.getUnit()));
        }
        return line.toString();
    }
}
