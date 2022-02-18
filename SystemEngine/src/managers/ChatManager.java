package managers;

import chat.SingleChatEntry;

import java.util.ArrayList;
import java.util.List;

public class ChatManager {
    private final List<SingleChatEntry> chatDataList;

    public ChatManager() {
        this.chatDataList = new ArrayList<>();
    }

    public synchronized void addChatString(String chatString, String username) {
        this.chatDataList.add(new SingleChatEntry(chatString, username));
    }

    public synchronized List<SingleChatEntry> getChatEntries(int fromIndex){
        if (fromIndex < 0 || fromIndex > this.chatDataList.size()) {
            fromIndex = 0;
        }
        return this.chatDataList.subList(fromIndex, this.chatDataList.size());
    }

    public int getVersion() {
        return this.chatDataList.size();
    }
}
