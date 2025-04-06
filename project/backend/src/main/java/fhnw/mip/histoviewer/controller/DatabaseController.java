package fhnw.mip.histoviewer.controller;

import fhnw.mip.histoviewer.model.DicomData;
import fhnw.mip.histoviewer.model.Tag;
import fhnw.mip.histoviewer.service.DatabaseService;
import fhnw.mip.histoviewer.service.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller class for handling database-related API requests.
 */
@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class DatabaseController {

    private final DatabaseService databaseService;
    private final TagService tagService;

    /**
     * Retrieves all image entities.
     * @return A list of all DicomData objects.
     */
    @GetMapping("/allImageEntities")
    public ResponseEntity<List<DicomData>> getAllImageEntities() {
        log.debug("Fetching all image entities");
        List<DicomData> images = databaseService.findAll();
        return images.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(images);
    }

    /**
     * Retrieves all tags.
     * @return A list of all Tag objects.
     */
    @GetMapping("/allTags")
    public ResponseEntity<List<Tag>> getAllTags() {
        log.debug("Fetching all tags");
        List<Tag> allTags = tagService.getAllTags();
        return allTags.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(allTags);
    }

    /**
     * Finds an image entity by UID.
     * @param uid The unique identifier of the image entity.
     * @return The corresponding DicomData object.
     */
    @GetMapping("/imageEntityByUid/{uid}")
    public ResponseEntity<DicomData> findImageEntityByUid(@PathVariable String uid){
        log.info("Received UID: {}", uid);
        DicomData dicomData = databaseService.findByUid(uid);
        return dicomData != null ? ResponseEntity.ok(dicomData) : ResponseEntity.notFound().build();
    }

    /**
     * Updates an image entity by UID.
     * @param uid The UID of the image entity.
     * @param username The username of the requester.
     * @param updates The update request body.
     * @return The updated DicomData object.
     */
    @PatchMapping(value = "/imageEntityByUid/{uid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DicomData> patchImageEntityByUid(
            @PathVariable @NotBlank @Size(min = 20) String uid,
            @RequestParam @NotBlank String username,  // Added username as a query parameter
            @Valid @RequestBody Map<String, Object> updates) {
        DicomData dicomImgToPatch = databaseService.updateByUid(uid, updates, username);
        return ResponseEntity.ok(dicomImgToPatch);
    }

    /**
     * Searches for image entities based on criteria.
     * @param searchCriteria The search criteria.
     * @param username The username of the requester.
     * @return A list of matching DicomData objects.
     */
    @PostMapping("/search")
    public ResponseEntity<List<DicomData>> findImageIdByCriteria(
            @Valid @RequestBody Map<String, String> searchCriteria,
            @RequestParam @NotBlank String username) {
        log.info("Searching images by criteria: {} for user: {}", searchCriteria, username);
        List<DicomData> searchResult = databaseService.findUidByCriteria(searchCriteria, username);
        return searchResult.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(searchResult);
    }

    /**
     * Deletes a comment by ID.
     * @param commentId The ID of the comment to delete.
     * @return A success message.
     */
    @DeleteMapping("/commentById")
    public ResponseEntity<String> deleteCommentById(@RequestParam("commentID") Long commentId) {
        log.info("Deleting comment with ID: {}", commentId);
        databaseService.deleteCommentById(commentId);
        return ResponseEntity.ok("Comment " + commentId + " deleted.");
    }

    /**
     * Removes a specific tag from a given DicomData entity using its UID.
     * If the tag is no longer referenced by any DicomData, it is also deleted from the database.
     *
     * @param dicomDataUid The UID of the DicomData entity from which the tag should be removed.
     * @param tagId The ID of the tag to be removed.
     * @return A ResponseEntity with a success message.
     */
    @DeleteMapping("/removeTagFromDicomData")
    public ResponseEntity<String> removeTagFromDicomData(
            @RequestParam("dicomDataUID") String dicomDataUid,
            @RequestParam("tagID") Long tagId) {

        log.info("Removing tag with ID: {} from DicomData with UID: {}", tagId, dicomDataUid);

        tagService.deleteTagFromDicomDataByUid(dicomDataUid, tagId);

        return ResponseEntity.ok("Tag " + tagId + " removed from DicomData " + dicomDataUid);
    }

    /**
     * Endpoint to delete a tag from all DicomData entities and delete the tag itself if it's no longer referenced.
     *
     * @param tagId The ID of the tag to be deleted.
     * @return ResponseEntity with a success message.
     */
    @DeleteMapping("/deleteTagFromAllDicomData")
    public ResponseEntity<String> deleteTagFromAllDicomData(
            @RequestParam("tagID") Long tagId) {

        log.info("Deleting tag with ID: {} from all DicomData", tagId);

        // Call the service method to delete the tag from all DicomData and remove the tag itself if necessary
        tagService.deleteTagFromAllDicomDataByUid(tagId);

        return ResponseEntity.ok("Tag " + tagId + " deleted from all DicomData and removed from the repository.");
    }

    /**
     * Replaces tags with a new tag (Admin only).
     * @param tag1 The first tag to be replaced.
     * @param tag2 The second tag to be replaced.
     * @param newTag The new tag to replace them with.
     * @return A success message.
     */
    @PostMapping("/replaceTags")
    public ResponseEntity<String> replaceTags(
            @RequestParam @NotBlank String tag1,
            @RequestParam @NotBlank String tag2,
            @RequestParam @NotBlank String newTag) {
        log.info("Replacing tags {} and {} with {}", tag1, tag2, newTag);
        tagService.replaceTags(tag1, tag2, newTag);
        return ResponseEntity.ok("Tags replaced successfully");
    }
}
