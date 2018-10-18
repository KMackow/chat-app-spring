package chat.server;

import java.util.*;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@RestController
public class ServerController {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private SimpMessagingTemplate simpMessagingTemplate;

    ServerController(UserRepository userRepository, ChatRepository chatRepository,
                     SimpMessagingTemplate simpMessagingTemplate) {

        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;

    }

    private Chat newChat(List<String> chatUsers, String creator) {
        Map<String, Integer> usersLastSeen = new HashMap<>();
        for (String user: chatUsers) {
            usersLastSeen.put(user, 0);
        }
        Chat chat = Chat.builder().users(chatUsers)
                .usersLastSeen(usersLastSeen)
                .messageHistory(new ArrayList<ChatMessage>())
                .build();
        chat.getMessageHistory().add(ChatMessage.builder()
                .author("System message")
                .authorId("System message")
                .authorAvatarUrl("").message("Chat created by " + creator)
                .timestamp(Long.toString(System.currentTimeMillis())).build());
        return chat;
    }

    @GetMapping("/api/user")
    public User user(OAuth2Authentication authentication) {
        Map<String, String> userDetails = (Map<String, String>) authentication.getUserAuthentication().getDetails();
        User newUser = User.builder()
                .id(userDetails.get("login"))
                .name(userDetails.get("name"))
                .avatarUrl(userDetails.get("avatar_url"))
                .build();
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
        if (topic.equals("/topic/allChats/" + user)) {
            List<UserChatsList> allUserChats = new ArrayList<>();
            List<Chat> chats = chatRepository.findAllByUsers(user);
            Collections.sort(chats, new ChatComparator());
            for (Chat chat : chats) {
                int noOfNotSeen = chat.getMessageHistory().size() - chat.getUsersLastSeen().get(user);
                allUserChats.add(new UserChatsList(chat.users, noOfNotSeen));
            }
            this.simpMessagingTemplate.convertAndSend("/topic/allChats/" + user, allUserChats);
        }
    }

    @MessageMapping("/{userId}")
    public void newMessage(@DestinationVariable String userId,
                           @Payload Map<String, Map<String, String>> message) throws Exception {

        List<String> chatUsers = new ArrayList<String>(message.get("chatUsers").values());
        Collections.sort(chatUsers);
        Map<String, String> author = message.get("author");
        ChatMessage newMessage = ChatMessage.builder()
                .author(author.get("name"))
                .authorId(author.get("id"))
                .authorAvatarUrl(author.get("avatarUrl"))
                .message(message.get("message").get("message"))
                .timestamp(Long.toString(System.currentTimeMillis()))
                .build();
        Chat chat = chatRepository.findByUsers(chatUsers);
        if (chat == null) {
            chat = newChat(chatUsers, author.get("id"));
        }
        chat.getMessageHistory().add(newMessage);
        chat.getUsersLastSeen().put(author.get("id"), chat.getMessageHistory().size());
        chatRepository.save(chat);
        for (String user: chat.getUsers()) {
            ReturnMessage returnMessage = ReturnMessage.builder().message(newMessage)
                    .users(chatUsers)
                    .noOfNotSeen(chat.getMessageHistory().size() - chat.getUsersLastSeen().get(user))
                    .build();
            this.simpMessagingTemplate.convertAndSend("/topic/" + user, returnMessage);
        }

    }

    @MessageMapping("/history")
    public void messageHistory(@Payload Map<String, Map<String, String>> chatDetails) throws Exception {
        List<String> chatUsers = new ArrayList<String>(chatDetails.get("chatUsers").values());
        Collections.sort(chatUsers);
        String user = chatDetails.get("user").get("user");
        int noOfMessagesRequested = Integer.parseInt(chatDetails.get("noOfMessages").get("noOfMessages"));
        Chat chat = chatRepository.findByUsers(chatUsers);
        if (chat == null) {
            chat = newChat(chatUsers, user);
            chat.getUsersLastSeen().put(user, chat.getMessageHistory().size());
            for (String eachUser: chat.getUsers()) {
                ReturnMessage returnMessage = ReturnMessage.builder()
                        .message(chat.getMessageHistory().get(0))
                        .users(chat.getUsers())
                        .noOfNotSeen(chat.getUsersLastSeen().get(user))
                        .build();
                this.simpMessagingTemplate.convertAndSend("/topic/newChat/" + eachUser, returnMessage);
                }
        }
        int totalNoOfMessages = chat.getMessageHistory().size();
        int noOfNotSeen = totalNoOfMessages - chat.getUsersLastSeen().get(user);
        int from = Math.max(Math.min(totalNoOfMessages - noOfNotSeen,
                totalNoOfMessages - noOfMessagesRequested), 0);
        int to = totalNoOfMessages;
        if (noOfMessagesRequested > 10) {
            to = Math.max(totalNoOfMessages - noOfMessagesRequested / 2, 0);
        }
        List<ChatMessage> history = chat.getMessageHistory().subList(from, to);
        chat.getUsersLastSeen().put(user, chat.getMessageHistory().size());
        chatRepository.save(chat);
        this.simpMessagingTemplate.convertAndSend("/topic/history/" + user, history);
    }

    @MessageMapping("/updateSeen/{userId}")
    public void updateSeen(@DestinationVariable String userId, @Payload String users) throws Exception {
        List<String> chatUsers = Arrays.asList(users.split(","));
        Chat chat = chatRepository.findByUsers(chatUsers);
        chat.getUsersLastSeen().put(userId, chat.getMessageHistory().size());
        chatRepository.save(chat);
    }
}
