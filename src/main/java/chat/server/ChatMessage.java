package chat.server;

import lombok.Data;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Data
@Document(collection = "messages")
public class ChatMessage {
    String authorId;
    String author;
    String authorAvatarUrl;
    String message;
    String timestamp;

    public ChatMessage() {}

    public ChatMessage(String author, String authorId, String authorAvatarUrl, String message, String timestamp) {

        this.author = author;
        this.authorId = authorId;
        this.authorAvatarUrl = authorAvatarUrl;
        this.message = message;
        this.timestamp = timestamp;
    }
}
