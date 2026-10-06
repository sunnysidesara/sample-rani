# Barangay Assistance System

A Java Swing desktop application implementing the provided UML class diagram:
User hierarchy (Resident / Admin / Staff -> MedicalStaff, FoodStaff,
TransportationStaff), the AssistanceRequest hierarchy (MedicalRequest,
FoodRequest, TransportationRequest), and a FileManager that persists
everything to local text files.

## How to compile and run

Requires JDK 11+ (uses `java.time`, records-free, tested on JDK 21).

```
cd src
javac -d ../bin *.java
cd ../bin
java Main
```

On first run, `data/users.txt` and `data/requests.txt` are created and
seeded with sample accounts:

| Role                | Name           | Password |
| ------------------- | -------------- | -------- |
| Admin               | admin          | admin123 |
| MedicalStaff        | Dr. Santos     | med123   |
| FoodStaff           | Aling Nena     | food123  |
| TransportationStaff | Mang Tomas     | trans123 |
| Resident            | Juan Dela Cruz | res123   |

New residents can also self-register from the login screen.

## Files

Every UML class lives in its own `.java` file under `src/`:

- `User.java`, `Resident.java`, `Admin.java`, `Staff.java`,
  `MedicalStaff.java`, `FoodStaff.java`, `TransportationStaff.java`
- `AssistanceRequest.java`, `MedicalRequest.java`, `FoodRequest.java`,
  `TransportationRequest.java`
- `RequestManager.java`, `FileManager.java`, `Main.java`
- GUI: `LoginFrame.java`, `RegisterFrame.java`, `ResidentMenuFrame.java`,
  `AdminMenuFrame.java`, `StaffMenuFrame.java`

## Design notes

- The model APIs follow the UML signatures, including parameterized resident
  request actions and admin user management. GUI dialogs and input validation
  remain in the matching `*MenuFrame` classes, which call those model APIs.
- `User.login()` completes authentication after `LoginFrame` verifies the
  supplied password through the helper `authenticate(String)` method.
- `displayDetails()` prints to console per the diagram; a
  `toDisplaySummary(): String` helper (overridden per subclass, i.e.
  polymorphism) feeds the same text into GUI tables/dialogs.
- Data is stored as pipe-delimited (`|`) text under `data/users.txt` and
  `data/requests.txt` so it survives restarts, satisfying the
  FileManager's read/save/update/delete requirements.
