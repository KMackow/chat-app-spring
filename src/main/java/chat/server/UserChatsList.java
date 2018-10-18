package chat.server;

import lombok.Data;

import java.util.List;

@Data
public class UserChatsList {

    List<String> users;
    int noOfNotSeen;

    UserChatsList() {}

    UserChatsList(List<String> users, int noOfNotSeen) {

        this.users = users;
        this.noOfNotSeen = noOfNotSeen;
    }
}
