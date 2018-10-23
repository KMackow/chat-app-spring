package chat.server;

import lombok.Builder;
import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Document(collection = "chats")
public class Chat {
    @Id
    String Id;
    List<String> users;
    Map<String, Integer> usersLastSeen;
    List<ChatMessage> messageHistory;

    public Chat() {}

    public Chat(List<String> users, String creator) {

        Map<String, Integer> usersLastSeen = new HashMap<>();
        for (String user: users) {
            usersLastSeen.put(user, 0);
        }
        ChatMessage firstMessage = ChatMessage.builder()
                .author("System message")
                .authorId("System message")
                .authorAvatarUrl("").message("Chat created by " + creator)
                .timestamp(Long.toString(System.currentTimeMillis()))
                .build();
        List<ChatMessage> history = new ArrayList<ChatMessage>();
        history.add(firstMessage);
        this.users = users;
        this.usersLastSeen = usersLastSeen;
        this.messageHistory = history;

    }

    public Chat(List<String> users, Map<String, Integer> userLastSeen, List<ChatMessage> messageHistory) {

        this.users = users;
        this.usersLastSeen = userLastSeen;
        this.messageHistory = messageHistory;
    }

    public static class ChatBuilder {
        public Chat build() {
            return new Chat(this.users, this.usersLastSeen, this.messageHistory);
        }
    }
}
