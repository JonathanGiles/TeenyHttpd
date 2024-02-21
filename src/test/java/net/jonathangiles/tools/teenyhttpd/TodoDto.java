package net.jonathangiles.tools.teenyhttpd;

public class TodoDto {

    private String uuid;
    private String title;
    private String description;
    private long timestamp;

    public TodoDto() {
    }

    public TodoDto(String uuid, String title, String description, long timestamp) {
        this.uuid = uuid;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public TodoDto setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public TodoDto setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TodoDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public TodoDto setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
