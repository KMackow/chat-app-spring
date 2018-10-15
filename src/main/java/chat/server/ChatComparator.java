package chat.server;

import java.util.Comparator;

public class ChatComparator implements Comparator<Chat> {
    @Override
    public int compare(Chat chat1, Chat chat2) {
        Long chat1Message = Long.parseLong(chat1.getMessageHistory().get(chat1.getMessageHistory().size() - 1).getTimestamp());
        Long chat2Message = Long.parseLong(chat2.getMessageHistory().get(chat2.getMessageHistory().size() - 1).getTimestamp());
        return chat2Message.compareTo(chat1Message);
    }
}
