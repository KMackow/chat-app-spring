package chat.server;

import lombok.Data;

import java.util.List;

@Data
public class NotSeenMessage {

    List<String> users;
    int noOfNotSeen;

    NotSeenMessage() {}

    NotSeenMessage(List<String> users, int noOfNotSeen) {

        this.users = users;
        this.noOfNotSeen = noOfNotSeen;
    }
}
