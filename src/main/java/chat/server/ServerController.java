package chat.server;

import java.util.*;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@RestController
public class ServerController {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRepository chatRepository;
    private SimpMessagingTemplate simpMessagingTemplate;

    ServerController(UserRepository userRepository, ChatMessageRepository chatMessageRepository,
                     ChatRepository chatRepository, SimpMessagingTemplate simpMessagingTemplate) {

        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRepository = chatRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;

    }

    @GetMapping("/api/user")
    public User user(OAuth2Authentication authentication) {
        Map<String, String> userDetails = (Map<String, String>) authentication.getUserAuthentication().getDetails();
        User newUser = new User(userDetails.get("login"), userDetails.get("name"), userDetails.get("avatar_url"));
        return userRepository.save(newUser);
    }

    @GetMapping("/api/users")
    List<User> getUsers() {
        return userRepository.findAll();
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        String topic = SimpMessageHeaderAccessor.wrap(event.getMessage()).getDestination();
        String user = event.getUser().getName();
        System.out.println("test1");
        if (topic.equals("/topic/newMessages/" + user)) {
            System.out.println(("test2"));
            this.simpMessagingTemplate.convertAndSend("/topic/newMessages/" + user, "GREETINGS");
        }
    }

    @MessageMapping("/{userId}")
    public void newMessage(@DestinationVariable String userId,
                           @Payload Map<String, Map<String, String>> message) throws Exception {

        List<String> chatUsers = new ArrayList<String>(message.get("chatUsers").values());
        Collections.sort(chatUsers);
        Chat chat = chatRepository.findByUsers(chatUsers);
        if (chat == null) {
            Map<String, Integer> usersMap = new HashMap<>();
            for (String user: chatUsers) {
                usersMap.put(user, 0);
            }
            Chat temp = new Chat(chatUsers, usersMap, new ChatMessage[]{});
            System.out.println(temp);
            chatRepository.save(temp);
        }
        System.out.println(chat);
        System.out.println(message);
        Map<String, String> author = message.get("author");
        ChatMessage newMessage = new ChatMessage(author.get("user"), author.get("id"),
                author.get("avatarUrl"), message.get("message").get("message"),
                Long.toString(System.currentTimeMillis()));
        chatMessageRepository.insert(newMessage);
        this.simpMessagingTemplate.convertAndSend("/topic/" + message.get("authorId"), newMessage);
    }
//    public Map<String, String> post(@Payload Map<String, String> message) {
//        System.out.println("test1234");
//        message.put("timestamp", Long.toString(System.currentTimeMillis()));
//        return message;
//    }

}
