package fhnw.mip.histoviewer.controller;

import fhnw.mip.histoviewer.model.User;
import fhnw.mip.histoviewer.repository.UserRepository;
import fhnw.mip.histoviewer.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller class for handling user-related API requests.
 */
@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Retrieves all users.
     * @return A list of all users.
     */
    @GetMapping("/allUsers")
    public List<User> allUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }

    /**
     * Retrieves the last search query of a user.
     * @param username The username of the user.
     * @return The last search query.
     */
    @GetMapping("/lastSearch")
    public ResponseEntity<String> getLastSearch(@RequestParam String username) {
        log.debug("Fetching last search for user: {}", username);
        User user = userRepository.findUserByUsername(username).orElse(null);

        if (user == null) {
            log.warn("User not found with username: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with username: " + username);
        }
        return ResponseEntity.ok(user.getLastSearch());
    }

    /**
     * Retrieves the last detailed image viewed by a user.
     * @param username The username of the user.
     * @return The last detailed image UID.
     */
    @GetMapping("/lastDetailViewImage")
    public ResponseEntity<?> getLastImage(@RequestParam("username") String username) {
        log.debug("Fetching last viewed image for user: {}", username);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        String lastImage = user.getLastImage();
        if (lastImage == null || lastImage.isEmpty()) {
            log.warn("No last image found for user: {}", username);
            return ResponseEntity.status(404).body("User does not have a last image.");
        }
        return ResponseEntity.ok(lastImage);
    }

    /**
     * Retrieves a user by username or creates a new user if not found.
     * @param username The username.
     * @return The user object.
     */
    @GetMapping("/user")
    public ResponseEntity<User> getUserByUsername(@RequestParam String username) {
        log.debug("Fetching user by username: {}", username);
        Optional<User> existingUser = userService.getUserByName(username);

        if (existingUser.isPresent()) {
            return ResponseEntity.ok(existingUser.get());
        } else {
            log.info("User not found, creating new user: {}", username);
            User newUser = userService.addUser(username);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);  // Return created user with 201 status
        }
    }

    /**
     * Updates user information with given fields.
     * @param username The username.
     * @param fields The fields to update.
     * @return The updated user object.
     */
    @PatchMapping("/user")
    public ResponseEntity<User> patchUser(
            @RequestParam String username,
            @RequestBody Map<String, Object> fields) {
        log.info("Updating user: {} with fields: {}", username, fields);
        Optional<User> updatedUser = userService.updateUser(username, fields);

        return updatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * Deletes a user by username.
     * @param username The username.
     * @return HTTP 204 if successful, 404 if not found.
     */
    @DeleteMapping("/user")
    public ResponseEntity<Void> deleteUser(@RequestParam String username) {
        log.info("Deleting user: {}", username);
        boolean isDeleted = userService.deleteUser(username);

        return isDeleted ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Deletes all users from the database.
     * @return HTTP 204 response.
     */
    @DeleteMapping("/allUsers")
    public ResponseEntity<Void> deleteAllUsers() {
        userRepository.deleteAll();
        log.info("All users deleted successfully.");
        return ResponseEntity.noContent().build(); // 204 No Content for successful deletion
    }
}
