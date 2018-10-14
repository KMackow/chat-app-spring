package chat.server;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "chats")
public class Chat {
    @Id
    String Id;
    List<String> users;
    Map<String, Integer> usersLastSeen;
    List<ChatMessage> messageHistory;

    public Chat() {}

    public Chat(List<String> users, Map<String, Integer> userLastSeen, List<ChatMessage> messageHistory) {

        this.users = users;
        this.usersLastSeen = userLastSeen;
        this.messageHistory = messageHistory;
    }

}
