package chat.server;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface ChatRepository extends MongoRepository<Chat, String> {
    Chat findByUsers(List<String> users);
    List<Chat> findAllByUsers(String user);

}
