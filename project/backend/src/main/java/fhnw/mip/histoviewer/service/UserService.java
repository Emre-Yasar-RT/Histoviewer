package fhnw.mip.histoviewer.service;

import fhnw.mip.histoviewer.model.User;
import fhnw.mip.histoviewer.repository.CommentRepository;
import fhnw.mip.histoviewer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Service class for handling user-related operations such as retrieving, adding, updating, and deleting users.
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve.
     * @return An Optional containing the user if found, or empty if not.
     */
    public Optional<User> getUserByName(String username) {
        log.debug("Fetching user by username: {}", username);
        return userRepository.findUserByUsername(username);
    }

    /**
     * Adds a new user with default values.
     *
     * @param username The username of the new user to create.
     * @return The newly created User object.
     */
    public User addUser(String username) {
        log.info("Creating new user with username: {}", username);

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setDefaultSliderValue("50");  // Default slider value
        newUser.setDefaultMode("dark");     // Default mode
        newUser.setDefaultLanguage("de");   // Default language

        // Save the new user to the database
        User savedUser = userRepository.save(newUser);
        log.info("User created with ID: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * Updates an existing user by applying the provided fields.
     * Only fields that are present in the request will be updated.
     *
     * @param username The username of the user to update.
     * @param fields A map containing the fields to update.
     * @return An Optional containing the updated user if found and updated, or empty if the user doesn't exist.
     */
    public Optional<User> updateUser(String username, Map<String, Object> fields) {
        log.info("Updating user with username: {}", username);

        Optional<User> existingUser = userRepository.findUserByUsername(username);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // Check and apply only the fields that are present in the request body
            fields.forEach((key, value) -> {
                switch (key) {
                    case "username":
                        user.setUsername((String) value);
                        break;
                    case "lastSearch":
                        user.setLastSearch((String) value);
                        break;
                    case "lastImage":
                        user.setLastImage((String) value);
                        break;
                    case "defaultSliderValue":
                        user.setDefaultSliderValue((String) value);
                        break;
                    case "defaultMode":
                        user.setDefaultMode((String) value);
                        break;
                    case "defaultLanguage":
                        user.setDefaultLanguage((String) value);
                        break;
                    default:
                        log.warn("Unknown field {} found during user update", key);
                        break;
                }
            });

            User updatedUser = userRepository.save(user);
            log.info("User with username {} updated successfully", username);
            return Optional.of(updatedUser);
        } else {
            log.warn("User with username {} not found for update", username);
            return Optional.empty();  // User not found
        }
    }

    /**
     * Deletes a user and all associated comments.
     *
     * @param username The username of the user to delete.
     * @return True if the user was deleted, false if the user wasn't found.
     */
    @Transactional
    public boolean deleteUser(String username) {
        log.info("Attempting to delete user with username: {}", username);

        Optional<User> userOptional = userRepository.findUserByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Delete all comments associated with the user
            log.info("Deleting all comments for user with username: {}", username);
            commentRepository.deleteAllCommentsByUser(user);

            // Delete the user
            userRepository.delete(user);
            log.info("User with username: {} deleted successfully", username);
            return true;
        }

        log.warn("User with username {} not found for deletion", username);
        return false;  // User not found
    }
}