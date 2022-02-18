package chat;

public class SingleChatEntry {
    private final String chatString;
    private final String username;
    private final long time;

    public SingleChatEntry(String chatString, String username) {
        this.chatString = chatString;
        this.username = username;
        this.time = System.currentTimeMillis();
    }

    public String getChatString() {
        return this.chatString;
    }

    public long getTime() {
        return this.time;
    }

    public String getUsername() {
        return this.username;
    }

    @Override public String toString() {
        return (this.username != null ? this.username + ": " : "") + this.chatString;
    }
}
