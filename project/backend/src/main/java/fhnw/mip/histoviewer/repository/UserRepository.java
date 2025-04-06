package fhnw.mip.histoviewer.repository;

import fhnw.mip.histoviewer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for accessing User data in the database.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their username.
     *
     * @param username the username of the user to search for
     * @return an Optional containing the User if found, or an empty Optional if not found
     */
    Optional<User> findUserByUsername(String username);
}