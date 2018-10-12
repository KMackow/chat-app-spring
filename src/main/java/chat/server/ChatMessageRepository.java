package chat.server;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
}
