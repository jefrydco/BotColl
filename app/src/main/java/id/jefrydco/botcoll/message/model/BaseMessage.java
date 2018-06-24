package id.jefrydco.botcoll.message.model;

public class BaseMessage {
    private String id;
    private String message;
    private long createdAt;

    BaseMessage(String id, String message, long createdAt) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
