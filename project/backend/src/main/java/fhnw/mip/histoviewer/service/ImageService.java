package fhnw.mip.histoviewer.service;

import fhnw.mip.histoviewer.model.User;
import fhnw.mip.histoviewer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.InvalidPathException;

/**
 * Service class for handling image retrieval operations.
 * This service interacts with Orthanc for fetching images by UID and provides image preview functionality.
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class ImageService {
    @Value("${orthanc.url}")
    private String ORTHANC_URL;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    /**
     * Retrieves an image by its UID, updates the user's last image, and logs the request.
     *
     * @param imageUid The unique identifier (UID) of the image to retrieve.
     * @param username The username of the current user making the request.
     * @return The image data as a byte array.
     * @throws NullPointerException if the imageUid is null.
     */
    public byte[] getImageByUid(String imageUid, String username) {
        if (imageUid == null) {
            log.error("Image UID is null for user: {}", username);
            throw new NullPointerException("imageUid is null");
        }

        // Get the current user from the username, or create a new user if not found
        User currentUser = userRepository.findUserByUsername(username)
                .orElseGet(() -> {
                    // Create a new user if the user doesn't exist
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setLastSearch(""); // Initialize with empty or default value
                    newUser.setLastImage(imageUid); // Initialize with empty or default value if required
                    userRepository.save(newUser); // Save the new user
                    return newUser; // Return the newly created user
                });

        // Update the user's lastImage field with the current imageId
        currentUser.setLastImage(imageUid);

        // Save the updated user back to the database
        userRepository.save(currentUser);
        userRepository.flush();

        log.debug("Fetching image for UID: {} for user: {}", imageUid, username);

        try {
            // Make the request to fetch the image from Orthanc and return the image bytes
            return restTemplate.getForObject(
                    ORTHANC_URL + "/wado?requestType=WADO&contentType=application/dicom&objectUID=" +
                            imageUid + "&contentType=image/png", byte[].class);
        } catch (Exception e) {
            log.error("Failed to fetch image for UID: {} for user: {}", imageUid, username, e);
            throw new InvalidPathException(imageUid, "Failed to retrieve image from Orthanc");
        }
    }

    /**
     * Retrieves a preview image by its UID.
     *
     * @param imageUid The unique identifier (UID) of the image to retrieve.
     * @return The preview image data as a byte array.
     * @throws NullPointerException if the imageUid is null.
     */
    public byte[] getPreviewImageByUid(String imageUid) {
        if (imageUid == null) {
            log.error("Image UID is null");
            throw new NullPointerException("imageUid is null");
        }
        try {
            // Log the request for preview image fetch
            log.debug("Fetching preview image for UID: {}", imageUid);

            // Make the request to fetch the preview image from Orthanc and return the image bytes
            return restTemplate.getForObject(
                    ORTHANC_URL + "/wado?requestType=WADO&contentType=application/dicom&objectUID=" +
                            imageUid + "&contentType=image/jpeg", byte[].class);
        } catch (Exception e) {
            log.error("Failed to fetch preview image for UID: {}", imageUid, e);
            throw new InvalidPathException(imageUid, "Failed to retrieve preview image from Orthanc");
        }
    }
}
