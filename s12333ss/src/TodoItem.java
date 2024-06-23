public class TodoItem {
    private int id;
    private String description;
    private boolean completed;
    private String box;

    public TodoItem(int id, String description, boolean completed, String box) {
        this.id = id;
        this.description = description;
        this.completed = completed;
        this.box = box;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getBox() {
        return box;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setBox(String box) {
        this.box = box;
    }
}
