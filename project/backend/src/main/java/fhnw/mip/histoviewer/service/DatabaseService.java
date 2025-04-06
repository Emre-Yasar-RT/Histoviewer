package fhnw.mip.histoviewer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fhnw.mip.histoviewer.model.Comment;
import fhnw.mip.histoviewer.model.DicomData;
import fhnw.mip.histoviewer.model.Tag;
import fhnw.mip.histoviewer.model.User;
import fhnw.mip.histoviewer.repository.CommentRepository;
import fhnw.mip.histoviewer.repository.DicomDataRepository;
import fhnw.mip.histoviewer.repository.TagRepository;
import fhnw.mip.histoviewer.repository.UserRepository;
import fhnw.mip.histoviewer.specifications.DicomEntitySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing DICOM data, comments, tags, and user information in the database.
 * <p>
 * This service provides methods for performing various operations like retrieving DICOM data by UID,
 * updating DICOM data entities, handling XML file imports, and managing user comments and tags.
 * </p>
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class DatabaseService {

    private final DicomDataRepository dicomDataRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Value("${dicom.json.filepath}")
    private String jsonFilePath;

    /**
     * Finds all DICOM data entries stored in the database.
     *
     * @return A list of all DICOM data entries.
     */
    public List<DicomData> findAll() {
        return dicomDataRepository.findAll();
    }

    /**
     * Finds a DICOM data entry by its unique identifier (UID).
     *
     * @param uid The unique identifier (UID) of the DICOM data entry.
     * @return The DICOM data entry matching the given UID, or null if not found.
     */
    public DicomData findByUid(String uid) {
        return dicomDataRepository.findByUid(uid);
    }

    /**
     * Updates a DICOM data entry based on the provided updates map. It will update tags, comments,
     * and other fields as specified.
     *
     * @param uid The UID of the DICOM data entry to update.
     * @param updates A map containing the fields to update, with keys as field names and values as new values.
     * @param username The username of the person making the update.
     * @return The updated DICOM data entry.
     * @throws IllegalArgumentException If an unknown field is specified in the updates map.
     */
    @Transactional
    public DicomData updateByUid(String uid, Map<String, Object> updates, String username) {
        if (uid == null || uid.trim().isEmpty()) {
            log.error("UID is null or empty");
            throw new IllegalArgumentException("UID cannot be null or empty");
        }

        if (updates == null || updates.isEmpty()) {
            log.error("Updates map is null or empty");
            throw new IllegalArgumentException("Updates cannot be null or empty");
        }

        log.info("Starting update for DICOM data with UID: {}", uid);

        DicomData existingImage = dicomDataRepository.findByUid(uid);
        if (existingImage == null) {
            log.error("No DICOM data found for UID: {}", uid);
            throw new IllegalArgumentException("DICOM data with UID " + uid + " not found");
        }

        log.info("Found existing DICOM data for UID: {}", uid);

        updates.forEach((key, value) -> {
            switch (key) {
                case "author":
                    if (value instanceof String) {
                        log.info("Updating author for UID: {}", uid);
                        existingImage.setAuthor((String) value);
                    } else {
                        log.error("Invalid value type for 'author': {}", value.getClass().getSimpleName());
                        throw new IllegalArgumentException("Invalid value type for 'author'");
                    }
                    break;

                case "tags":
                    if (value instanceof List<?>) {
                        log.info("Updating tags for UID: {}", uid);
                        List<Map<String, Object>> tagsData = (List<Map<String, Object>>) value;
                        tagsData.forEach(tagData -> {
                            if (tagData.get("name") instanceof String) {
                                String tagName = (String) tagData.get("name");

                                // Get the tag list and use the first one if available, otherwise create a new one
                                List<Tag> existingTags = tagRepository.findByName(tagName);
                                Tag tag = existingTags.isEmpty() ? new Tag() : existingTags.get(0);

                                if (existingTags.isEmpty()) {
                                    tag.setName(tagName);
                                    log.info("Creating new tag: {}", tagName);
                                    tag = tagRepository.save(tag); // Save new tag immediately
                                }

                                // Establish bidirectional relationships
                                existingImage.getTags().add(tag);
                                tag.getDicomData().add(existingImage);
                            } else {
                                log.error("Invalid tag name: {}", tagData.get("name"));
                                throw new IllegalArgumentException("Tag name must be a string");
                            }
                        });
                    } else {
                        log.error("Invalid value type for 'tags': {}", value.getClass().getSimpleName());
                        throw new IllegalArgumentException("Invalid value type for 'tags'");
                    }
                    break;

                case "comments":
                    if (value instanceof List<?>) {
                        log.info("Updating comments for UID: {}", uid);
                        List<Map<String, Object>> commentsData = (List<Map<String, Object>>) value;

                        // Retrieve user once to avoid repeated queries
                        User user = userRepository.findUserByUsername(username)
                                .orElseGet(() -> {
                                    User newUser = new User();
                                    newUser.setUsername(username);
                                    newUser.setLastSearch("default search");
                                    newUser.setLastImage("default image");
                                    log.info("Creating new user with username: {}", username);
                                    return userRepository.save(newUser);
                                });

                        commentsData.forEach(commentData -> {
                            if (commentData.get("text") instanceof String) {
                                Comment comment = new Comment();
                                comment.setText((String) commentData.get("text"));
                                comment.setCreatedAt(LocalDateTime.now());
                                comment.setDicomData(existingImage);
                                comment.setUser(user);

                                existingImage.getComments().add(comment);
                                log.info("Added comment for UID: {}", uid);
                            } else {
                                log.error("Invalid comment text: {}", commentData.get("text"));
                                throw new IllegalArgumentException("Comment text must be a string");
                            }
                        });
                    } else {
                        log.error("Invalid value type for 'comments': {}", value.getClass().getSimpleName());
                        throw new IllegalArgumentException("Invalid value type for 'comments'");
                    }
                    break;

                default:
                    log.error("Unknown field: {}", key);
                    throw new IllegalArgumentException("Unknown field: " + key);
            }
        });

        log.info("Saving updated DICOM data for UID: {}", uid);
        DicomData updatedImage = dicomDataRepository.save(existingImage);

        log.info("Successfully updated DICOM data for UID: {}", uid);
        return updatedImage;
    }

    /**
     * Deletes a DicomData entity by its ID.
     *
     * @param id The ID of the DicomData entity to delete.
     * @return true if the entity was found and deleted, false otherwise.
     */
    public boolean deleteImageEntityById(Long id) {
        if (id == null) {
            log.warn("Attempted to delete a DicomData entity with a null ID.");
            return false;
        }

        Optional<DicomData> dicomData = dicomDataRepository.findById(id);

        if (dicomData.isPresent()) {
            dicomDataRepository.deleteById(id);
            return true; // Return true if the entity was found and deleted
        }
        log.warn("DicomData entity with ID {} not found. Deletion skipped.", id);
        return false; // Return false if the entity was not found
    }

    /**
     * Deletes all DicomData entities from the repository.
     */
    public void deleteAllImageEntities() {
        log.info("Deleting all DicomData entities...");
        dicomDataRepository.deleteAll();
        log.info("All DicomData entities have been deleted successfully.");
    }

    /**
     * Finds DicomData entities based on search criteria and updates the user's last search history.
     *
     * @param searchCriteria The search parameters as a key-value map.
     * @param username       The username of the user performing the search.
     * @return A list of matching DicomData entities.
     */
    public List<DicomData> findUidByCriteria(Map<String, String> searchCriteria, String username) {
        log.info("Search Criteria: {}", searchCriteria);

        // Get the current user from the username, or create a new user if not found
        User currentUser = userRepository.findUserByUsername(username)
                .orElseGet(() -> {
                    // Create a new user if the user doesn't exist
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setLastSearch(convertSearchCriteriaToJson(searchCriteria)); // Initialize with empty or default value
                    newUser.setLastImage(""); // Initialize with empty or default value if required
                    userRepository.save(newUser); // Save the new user
                    return newUser; // Return the newly created user
                });

        // Update the user's last search
        currentUser.setLastSearch(convertSearchCriteriaToJson(searchCriteria));  // Convert map to string for storage
        currentUser.setLastImage("");
        userRepository.save(currentUser);  // Save the user with updated search criteria

        Specification<DicomData> spec = Specification.where(null);

        // Process description search
        if (StringUtils.hasText(searchCriteria.get("descriptionSearchTerm"))) {
            String[] searchTermList = searchCriteria.get("descriptionSearchTerm").split(" ");

            Specification<DicomData> descriptionSpec = Specification.where(null);
            for (String searchTerm : searchTermList) {
                log.info("Searching for description term: {}", searchTerm);
                descriptionSpec = descriptionSpec.or(DicomEntitySpecification.containsTermInDescription(searchTerm));
            }
            spec = spec.and(descriptionSpec);
        }

        // Process tag search
        if (StringUtils.hasText(searchCriteria.get("tagSearchTerm"))) {
            String[] tagSearchTerms = searchCriteria.get("tagSearchTerm").split(" "); // Split tags by comma (",")

            Specification<DicomData> tagSpec = Specification.where(null);
            for (String tagTerm : tagSearchTerms) {
                log.info("Filtering by tag: {}", tagTerm);
                tagSpec = tagSpec.or(DicomEntitySpecification.hasTag(tagTerm));

            }
            spec = spec.and(tagSpec);
        }

        // Process comments search
        if (StringUtils.hasText(searchCriteria.get("commentsSearchTerm"))) {
            log.info("Filtering by comment: {}", searchCriteria.get("commentsSearchTerm"));
            spec = spec.and(DicomEntitySpecification.containsTermInComments(searchCriteria.get("commentsSearchTerm")));
        }

        if (StringUtils.hasText(searchCriteria.get("colorSearchTerm"))) {
            log.info("Filtering by color: {}", searchCriteria.get("colorSearchTerm"));
            spec = spec.and(DicomEntitySpecification.hasColor(searchCriteria.get("colorSearchTerm")));
        }

        log.info("Specification built: {}", spec);

        return dicomDataRepository.findAll(spec);
    }


    /**
     * Converts search criteria map to a JSON string.
     *
     * @param searchCriteria The search parameters as a key-value map.
     * @return A JSON string representation of the search criteria.
     */
    private String convertSearchCriteriaToJson(Map<String, String> searchCriteria) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(searchCriteria);
        } catch (Exception e) {
            log.error("Error converting search criteria to JSON", e);
            return "{}"; // Return empty JSON object in case of error
        }
    }

    /**
     * Deletes a comment by its ID.
     *
     * @param commentId The ID of the comment to delete.
     */
    public void deleteCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        DicomData dicomData = comment.getDicomData();

        if (dicomData != null) {
            dicomData.getComments().remove(comment); // Remove from list
            dicomDataRepository.save(dicomData); // Update parent
        }

        commentRepository.delete(comment); // Delete comment
    }
}