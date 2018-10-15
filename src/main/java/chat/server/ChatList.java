package chat.server;

import lombok.Data;

import java.util.List;

@Data
public class ChatList {

    List<String> users;
    int noOfNotSeen;

    ChatList() {}

    ChatList(List<String> users, int noOfNotSeen) {

        this.users = users;
        this.noOfNotSeen = noOfNotSeen;
    }
}
