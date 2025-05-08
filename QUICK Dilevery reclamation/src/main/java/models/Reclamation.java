package models;

public class Reclamation {
    private int id;
    private int userId;
    private String description;
    private String dateCreation;
    private String response;
    private String priority;

    // Constructor
    public Reclamation(int id, int userId, String description,
                       String dateCreation, String response, String priority) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.dateCreation = dateCreation;
        this.response = response;
        this.priority = priority;
    }

    // Getters - MUST match PropertyValueFactory names in FXML!
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getDescription() { return description; }
    public String getDateCreation() { return dateCreation; }
    public String getResponse() { return response; }
    public String getPriority() { return priority; }
}