package fhnw.mip.histoviewer.controller;

import fhnw.mip.histoviewer.service.ImageService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 * Controller class for handling image-related API requests.
 */
@Validated
@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ImageController {

    @Value("${image.cache.duration}")
    private String imageCacheDuration;

    private final ImageService imageService;

    /**
     * Retrieves the detailed view of an image.
     * @param imageUid The unique identifier of the image.
     * @param username The username requesting the image.
     * @return The image data as a byte array.
     */
    @GetMapping("/detailViewImage")
    public ResponseEntity<?> detailViewImage(
            @RequestParam("imageUid") @NotBlank String imageUid,
            @RequestParam("username") @NotBlank String username) {
        log.debug("Fetching detailed image view for UID: {} by user: {}", imageUid, username);
        var imageToShow = imageService.getImageByUid(imageUid, username);
        Duration cacheDuration = parseDuration(imageCacheDuration);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheDuration))
                .contentLength(imageToShow.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(imageToShow);
    }

    /**
     * Retrieves the preview of an image.
     * @param imageUid The unique identifier of the image.
     * @return The image preview data as a byte array.
     */
    @GetMapping("/previewImage")
    public ResponseEntity<?> previewImage(
            @RequestParam("imageUid") @NotBlank String imageUid) {
        log.debug("Fetching preview image for UID: {}", imageUid);
        var imageToShow = imageService.getPreviewImageByUid(imageUid);
        Duration cacheDuration = parseDuration(imageCacheDuration);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheDuration))
                .contentLength(imageToShow.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(imageToShow);
    }

    /**
     * Parses the cache duration string to a Duration object.
     * Defaults to 1 hour if the format is invalid.
     * @param durationString The duration string from properties.
     * @return A valid Duration object.
     */
    private Duration parseDuration(String durationString) {
        try {
            return Duration.parse(durationString);
        } catch (DateTimeParseException e) {
            log.error("Invalid duration format: {}. Defaulting to PT1H.", durationString);
            return Duration.parse("PT1H"); // Default to 1 hour
        }
    }
}