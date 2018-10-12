package chat.server;

import lombok.Data;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "chats")
public class Chat {

    List<String> users;
    Map<String, Integer> usersLastSeen;
    ChatMessage[] messageHistory;

    public Chat() {}

    public Chat(List<String> users, Map<String, Integer> userLastSeen, ChatMessage[] messageHistory) {

        this.users = users;
        this.usersLastSeen = userLastSeen;
        this.messageHistory = messageHistory;
    }

}
