import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** Coordinates request operations and delegates persistence to FileManager. */
public class RequestManager {
    private final FileManager fileManager;
    private final Map<String, AssistanceRequest> selectedRequests = new HashMap<>();

    public RequestManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public void addRequest(AssistanceRequest request) {
        fileManager.saveRequest(request);
    }

    public String nextRequestId() {
        return fileManager.nextRequestId();
    }

    public AssistanceRequest findRequest(String requestId) {
        for (AssistanceRequest request : getAllRequests()) {
            if (request.getRequestId().equals(requestId)) {
                selectedRequests.put(requestId, request);
                return request;
            }
        }
        return null;
    }

    public List<AssistanceRequest> getAllRequests() {
        return fileManager.readRequests();
    }

    public void updateRequest(String requestId) {
        AssistanceRequest request = selectedRequests.remove(requestId);
        if (request == null) request = findRequest(requestId);
        if (request != null) fileManager.updateRequestFile(request);
    }

    public void deleteRequest(String requestId) {
        selectedRequests.remove(requestId);
        fileManager.deleteRequestFromFile(requestId);
    }
}
