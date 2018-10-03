package chat.server;

public class UserNotFoundException extends RuntimeException {

    private String userID;

    UserNotFoundException(String id) {
        this.userID = id;
    }

    public String getMessage() {
        return "Could not find user " + this.userID;
    }
}
