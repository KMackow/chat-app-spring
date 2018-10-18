package chat.server;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReturnMessage {

    ChatMessage message;
    private List<String> users;
    int noOfNotSeen;

    ReturnMessage() {}

    ReturnMessage(String author, String authorId, String authorAvatarUrl,
                  String message, String timestamp, List<String> users, int noOfNotSeen) {
        this.message = new ChatMessage(author, authorId, authorAvatarUrl, message, timestamp);
        this.users = users;
        this.noOfNotSeen = noOfNotSeen;
    }

    ReturnMessage(ChatMessage chatmessage, List<String> users, int noOfNotSeen) {
        this.message = chatmessage;
        this.users = users;
        this.noOfNotSeen = noOfNotSeen;
    }
}
