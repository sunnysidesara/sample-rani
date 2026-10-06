import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * Handles all local file persistence for users and assistance requests.
 * Data is stored as pipe-delimited text files under the "data" folder.
 */
public class FileManager {
    private static final String DATA_DIR = "data";
    private static final String USER_FILE = DATA_DIR + File.separator + "users.txt";
    private static final String REQUEST_FILE = DATA_DIR + File.separator + "requests.txt";
    private static final String STAFF_HISTORY_FILE = DATA_DIR + File.separator + "staff_history.txt";
    private static final String RESIDENT_HISTORY_FILE = DATA_DIR + File.separator + "resident_history.txt";
    private final Map<String, AssistanceRequest> stagedRequestUpdates = new HashMap<>();

    public FileManager() {
        ensureDataFiles();
    }

    private void ensureDataFiles() {
        try {
            Path dataDirectory = Paths.get(DATA_DIR);
            if (Files.notExists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            createIfMissing(Paths.get(USER_FILE));
            createIfMissing(Paths.get(REQUEST_FILE));
            createIfMissing(Paths.get(STAFF_HISTORY_FILE));
            createIfMissing(Paths.get(RESIDENT_HISTORY_FILE));
        } catch (IOException e) {
            System.err.println("Could not initialize data files: " + e.getMessage());
        }
    }

    private void createIfMissing(Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
    }

    private void appendLine(Path path, String line) {
        try (BufferedWriter bw = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error writing file entry: " + e.getMessage());
        }
    }

    private void writeLines(Path path, Iterable<String> lines) {
        try (BufferedWriter bw = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) continue;
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error rewriting file: " + e.getMessage());
        }
    }

    private <T> List<T> readRecords(Path path, Function<String, T> parser) {
        List<T> records = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(path)) {
                if (line.trim().isEmpty()) continue;
                T record = parser.apply(line);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading records: " + e.getMessage());
        }
        return records;
    }

    private String generateNextId(String prefix, long minimum, Collection<String> ids) {
        long highestId = minimum;
        for (String id : ids) {
            if (id == null || !id.toUpperCase().startsWith(prefix)) continue;
            String numericPart = id.substring(prefix.length());
            if (numericPart.length() >= 12) continue;
            try {
                highestId = Math.max(highestId, Long.parseLong(numericPart));
            } catch (NumberFormatException ignored) {
                // Ignore malformed IDs that do not match the expected numeric format.
            }
        }
        return prefix + (highestId + 1);
    }

    // ---------------------------------------------------------------- USERS

    /** Reads and reconstructs every user account from disk. */
    public List<User> readUsers() {
        return readRecords(Paths.get(USER_FILE), this::parseUserLine);
    }

    private User parseUserLine(String line) {
        String[] f = line.split("\\|", -1);
        try {
            switch (f[0]) {
                case "Resident":
                    return new Resident(f[1], f[2], f[3], f[4], f[5]);
                case "Admin":
                    return new Admin(f[1], f[2], f[3]);
                case "MedicalStaff":
                    return new MedicalStaff(f[1], f[2], f[3]);
                case "FoodStaff":
                    return new FoodStaff(f[1], f[2], f[3]);
                case "TransportationStaff":
                    return new TransportationStaff(f[1], f[2], f[3]);
                case "Staff":
                    return new Staff(f[1], f[2], f[3], f[4]);
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Skipping malformed user line: " + line);
            return null;
        }
    }

    /** Appends one new user to the users file. */
    public void saveUser(User user) {
        appendLine(Paths.get(USER_FILE), user.toFileLine());
    }

    /** Returns the next sequential resident ID after the highest existing RSD ID. */
    public String nextResidentId() {
        List<String> ids = new ArrayList<>();
        for (User user : readUsers()) {
            if (user instanceof Resident) {
                ids.add(((Resident) user).getResidentId());
            }
        }
        return generateNextId("RSD", 3000, ids);
    }

    /** Rewrites the entire users file (used after edits or deletes). */
    public void saveAllUsers(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(user.toFileLine());
        }
        writeLines(Paths.get(USER_FILE), lines);
    }

    // ------------------------------------------------------------ REQUESTS

    /** Reads and reconstructs every assistance request from disk. */
    public List<AssistanceRequest> readRequests() {
        return readRecords(Paths.get(REQUEST_FILE), this::parseRequestLine);
    }

    /** Returns the next sequential request ID after the highest existing REQ ID. */
    public String nextRequestId() {
        List<String> ids = new ArrayList<>();
        for (AssistanceRequest request : readRequests()) {
            ids.add(request.getRequestId());
        }
        return generateNextId("REQ", 3999, ids);
    }

    private AssistanceRequest parseRequestLine(String line) {
        String[] f = line.split("\\|", -1);
        try {
            LocalDateTime dateRequested = LocalDateTime.parse(f[6], AssistanceRequest.DT_FMT);
            switch (f[0]) {
                case "MEDICAL":
                    return new MedicalRequest(f[1], f[2], f[4], f[5], dateRequested, f[7], f[8]);
                case "FOOD":
                    return new FoodRequest(f[1], f[2], f[4], f[5], dateRequested, f[7], f[8]);
                case "TRANSPORT":
                    LocalDateTime whenNeeded = LocalDateTime.parse(f[9], AssistanceRequest.DT_FMT);
                    return new TransportationRequest(f[1], f[2], f[4], f[5], dateRequested, f[7], f[8], whenNeeded);
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Skipping malformed request line: " + line);
            return null;
        }
    }

    /** Appends one new request to the requests file. */
    public void saveRequest(AssistanceRequest request) {
        appendLine(Paths.get(REQUEST_FILE), request.toFileLine());
    }

    /** Stages the edited request and persists it using the UML request-ID API. */
    public void updateRequestFile(AssistanceRequest updated) {
        stagedRequestUpdates.put(updated.getRequestId(), updated);
        updateRequestFile(updated.getRequestId());
    }

    /** Finds the request with this ID and persists its staged changes. */
    public void updateRequestFile(String requestId) {
        List<AssistanceRequest> all = readRequests();
        AssistanceRequest updated = stagedRequestUpdates.remove(requestId);
        if (updated == null) {
            for (AssistanceRequest request : all) {
                if (request.getRequestId().equals(requestId)) {
                    updated = request;
                    break;
                }
            }
        }
        if (updated == null) return;

        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getRequestId().equals(requestId)) {
                all.set(i, updated);
                break;
            }
        }
        rewriteRequests(all);
    }

    /** Keeps a resident's existing requests linked when their ID is edited. */
    public void updateResidentIdInRequests(String oldResidentId, String newResidentId) {
        List<AssistanceRequest> all = readRequests();
        for (AssistanceRequest request : all) {
            if (request.getResidentId().equals(oldResidentId)) {
                request.setResidentId(newResidentId);
            }
        }
        rewriteRequests(all);
        updateResidentIdInHistory(oldResidentId, newResidentId);
    }

    /** Records a snapshot whenever a resident edits or cancels a request. */
    public void recordResidentRequestAction(Resident resident, AssistanceRequest request, String action) {
        ResidentRequestHistory history = new ResidentRequestHistory(resident.getResidentId(),
                request.getRequestId(), request.getType(), action, request.toDisplaySummary(), LocalDateTime.now());
        appendLine(Paths.get(RESIDENT_HISTORY_FILE), history.toFileLine());
    }

    /** Reads resident request edit and cancellation history. */
    public List<ResidentRequestHistory> readResidentRequestHistory() {
        List<ResidentRequestHistory> history = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(Paths.get(RESIDENT_HISTORY_FILE))) {
                if (line.trim().isEmpty()) continue;
                ResidentRequestHistory entry = ResidentRequestHistory.fromFileLine(line);
                if (entry != null) {
                    history.add(entry);
                } else {
                    System.err.println("Skipping malformed resident history line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading resident request history: " + e.getMessage());
        }
        return history;
    }

    private void updateResidentIdInHistory(String oldResidentId, String newResidentId) {
        List<ResidentRequestHistory> history = readResidentRequestHistory();
        List<String> lines = new ArrayList<>();
        for (ResidentRequestHistory entry : history) {
            if (entry.getResidentId().equals(oldResidentId)) {
                entry.setResidentId(newResidentId);
            }
            lines.add(entry.toFileLine());
        }
        writeLines(Paths.get(RESIDENT_HISTORY_FILE), lines);
    }

    /** Records a staff action so it remains available even if the request is later deleted. */
    public void recordStaffRequestAction(Staff staff, AssistanceRequest request, String status) {
        StaffRequestHistory history = new StaffRequestHistory(staff.getStaffId(), request.getRequestId(),
                request.getResidentId(), request.getType(), status, LocalDateTime.now());
        appendLine(Paths.get(STAFF_HISTORY_FILE), history.toFileLine());
    }

    /** Reads all recorded staff actions. */
    public List<StaffRequestHistory> readStaffRequestHistory() {
        List<StaffRequestHistory> history = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(Paths.get(STAFF_HISTORY_FILE))) {
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split("\\|", -1);
                if (fields.length != 6) continue;
                try {
                    history.add(new StaffRequestHistory(fields[0], fields[1], fields[2], fields[3], fields[4],
                            LocalDateTime.parse(fields[5])));
                } catch (Exception e) {
                    System.err.println("Skipping malformed staff history line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading staff history: " + e.getMessage());
        }
        return history;
    }

    /** Keeps recorded actions linked when a staff ID is changed. */
    public void updateStaffIdInHistory(String oldStaffId, String newStaffId) {
        List<StaffRequestHistory> history = readStaffRequestHistory();
        List<String> lines = new ArrayList<>();
        for (StaffRequestHistory entry : history) {
            if (entry.getStaffId().equals(oldStaffId)) {
                entry.setStaffId(newStaffId);
            }
            lines.add(entry.toFileLine());
        }
        writeLines(Paths.get(STAFF_HISTORY_FILE), lines);
    }

    /** Removes the request with the given id from the file. */
    public void deleteRequestFromFile(String requestId) {
        List<AssistanceRequest> all = readRequests();
        all.removeIf(r -> r.getRequestId().equals(requestId));
        rewriteRequests(all);
    }

    private void rewriteRequests(List<AssistanceRequest> all) {
        List<String> lines = new ArrayList<>();
        for (AssistanceRequest request : all) {
            lines.add(request.toFileLine());
        }
        writeLines(Paths.get(REQUEST_FILE), lines);
    }
}
