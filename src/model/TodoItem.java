package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TodoItem {

    public String id;

    public String content;

    public LocalDateTime deadline;

    public Boolean deleted;

    public TodoItem(String content, LocalDateTime deadline) {
        this.id = this.generateId();
        this.content = content;
        this.deadline = deadline;
    }

    public TodoItem(String id, String content, LocalDateTime deadline) {
        this.id = id;
        this.content = content;
        this.deadline = deadline;
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
