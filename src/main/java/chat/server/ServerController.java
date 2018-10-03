package chat.server;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {

    private final UserRepository userRepository;

    ServerController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
