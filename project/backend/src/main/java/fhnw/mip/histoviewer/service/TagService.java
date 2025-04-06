package fhnw.mip.histoviewer.service;

import fhnw.mip.histoviewer.model.DicomData;
import fhnw.mip.histoviewer.model.Tag;
import fhnw.mip.histoviewer.repository.DicomDataRepository;
import fhnw.mip.histoviewer.repository.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final EntityManager em;
    private final DicomDataRepository dicomDataRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Retrieves all available tags from the repository.
     *
     * @return A list of all Tag entities.
     */
    public List<Tag> getAllTags() {
        log.debug("Fetching all tags from the repository.");
        return tagRepository.findAll();
    }

    /**
     * Replaces occurrences of two specified tags with a new tag across all DicomData entities.
     * If neither of the tags to be replaced are found, an exception is thrown.
     *
     * @param tag1   The first tag to be replaced.
     * @param tag2   The second tag to be replaced.
     * @param newTag The new tag to replace the existing tags.
     * @throws ResponseStatusException if neither tag is found or no affected DicomData entities exist.
     */
    @Transactional
    public void replaceTags(String tag1, String tag2, String newTag) {
        log.info("Replacing tags '{}' and '{}' with '{}'", tag1, tag2, newTag);

        // Find all existing tags matching tag1 and tag2
        List<Tag> tagsToRemove1 = tagRepository.findByName(tag1);
        List<Tag> tagsToRemove2 = tagRepository.findByName(tag2);

        log.info("Found {} instances of tag '{}' and {} instances of tag '{}'", tagsToRemove1.size(), tag1, tagsToRemove2.size(), tag2);

        // If neither tag is found, throw an exception
        if (tagsToRemove1.isEmpty() && tagsToRemove2.isEmpty()) {
            log.warn("Tags not found: '{}' or '{}'", tag1, tag2);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tags not found");
        }

        // Find all DicomData entities that have at least one of these tags
        List<DicomData> affectedDicomData = dicomDataRepository.findAll().stream()
                .filter(d -> d.getTags().stream().anyMatch(t -> tagsToRemove1.contains(t) || tagsToRemove2.contains(t)))
                .collect(Collectors.toList());

        log.info("Found {} DicomData entities affected by these tags", affectedDicomData.size());

        // If no DicomData entities are found, throw an exception
        if (affectedDicomData.isEmpty()) {
            log.warn("No DicomData entities found with tags '{}' or '{}'", tag1, tag2);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No DicomData entities found with these tags");
        }

        // Create or get the new tag to replace the old ones
        Tag replacementTag = tagRepository.findByName(newTag).stream().findFirst().orElseGet(() -> {
            log.info("Creating new tag '{}'", newTag);
            Tag newT = new Tag();
            newT.setName(newTag);
            return tagRepository.save(newT); // Persist the new tag in the database
        });

        // Update each DicomData entity that had the old tags
        for (DicomData dicom : affectedDicomData) {
            log.info("Updating DicomData entity with ID '{}'", dicom.getId());

            // Remove old tags from the DicomData entity
            dicom.getTags().removeIf(t -> tagsToRemove1.contains(t) || tagsToRemove2.contains(t));

            // Add the new tag if it is not already present
            if (!dicom.getTags().contains(replacementTag)) {
                log.info("Adding new tag '{}' to DicomData '{}'", newTag, dicom.getId());
                dicom.getTags().add(replacementTag);
            }

            // Ensure the bidirectional relationship is maintained
            if (!replacementTag.getDicomData().contains(dicom)) {
                replacementTag.getDicomData().add(dicom);
            }

            // Save the updated DicomData entity back to the database
            dicomDataRepository.save(dicom);
        }

        // Save the updated tag to ensure consistency
        tagRepository.save(replacementTag);

        // Remove old tags if they are no longer associated with any DicomData
        tagRepository.deleteAll(tagsToRemove1);
        tagRepository.deleteAll(tagsToRemove2);

        log.info("Tag replacement process completed successfully");
    }

    /**
     * Removes a tag from a DicomData entity using its UID.
     * If no other DicomData references the tag, it is also deleted from the database.
     *
     * @param dicomDataUid The UID of the DicomData entity from which the tag should be removed.
     * @param tagId The ID of the tag to be removed.
     */
    public void deleteTagFromDicomDataByUid(String dicomDataUid, Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        DicomData dicomData = dicomDataRepository.findByUid(dicomDataUid);
        if (dicomData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DicomData not found");
        }

        // Remove the tag from the DicomData
        dicomData.getTags().remove(tag);
        dicomDataRepository.save(dicomData); // Persist the updated DicomData

        // If no other DicomData references this tag, delete it
        if (tag.getDicomData().isEmpty()) {
            tagRepository.delete(tag);
        }
    }

    /**
     * Deletes a tag from all DicomData entities that reference it and deletes the tag itself.
     *
     * @param tagId The ID of the tag to be deleted.
     */
    @Transactional // Ensures EntityManager is within a transaction
    public void deleteTagFromAllDicomDataByUid(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        for (DicomData dicomData : new ArrayList<>(tag.getDicomData())) {
            dicomData.getTags().remove(tag);
            dicomDataRepository.save(dicomData);
        }
        tagRepository.delete(tag);
        entityManager.flush(); // Force immediate delete
    }
}