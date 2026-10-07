import java.util.List;

/** Coordinates request operations and delegates persistence to FileManager. */
public class RequestManager {
    private final FileManager fileManager;

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
                return request;
            }
        }
        return null;
    }

    public List<AssistanceRequest> getAllRequests() {
        return fileManager.readRequests();
    }

    public void updateRequest(AssistanceRequest request) {
        if (request != null) {
            fileManager.updateRequestFile(request);
        }
    }

    public void deleteRequest(String requestId) {
        fileManager.deleteRequestFromFile(requestId);
    }
}
