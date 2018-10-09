package chat.server;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RequestMapping(value = "/api")
@RestController
public class ServerController {

    private final UserRepository userRepository;

    ServerController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user")
    public User user(OAuth2Authentication authentication) {
        Map<String, String> userDetails = (Map<String, String>) authentication.getUserAuthentication().getDetails();
        User newUser = new User(userDetails.get("login"), userDetails.get("name"), userDetails.get("avatar_url"));
        return userRepository.save(newUser);
    }

    @GetMapping("/user{id}")
    User getUser(@RequestParam String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @GetMapping("/users")
    List<User> getUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/userTest")
    User newUser(@RequestBody User newUser) {
        return userRepository.save((newUser));
    }

}
