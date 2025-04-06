package fhnw.mip.histoviewer.service;

import fhnw.mip.histoviewer.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service class for handling administrative tasks related to image entities.
 */
@Log4j2
@Service
public class AdminService {

    private final DatabaseService databaseService;
    private final UserRepository userRepository;
    private final XmlParseService xmlParseService;

    /**
     * Constructor for {@code AdminService}.
     *
     * @param databaseService The service handling database operations.
     * @param userRepository The repository handling user operations.
     */
    public AdminService(DatabaseService databaseService, UserRepository userRepository, XmlParseService xmlParseService) {
        this.databaseService = databaseService;
        this.userRepository = userRepository;
        this.xmlParseService = xmlParseService;
    }

    /**
     * Deletes an image entity by its ID.
     *
     * @param id The ID of the image entity to delete.
     * @return {@code true} if deletion was successful, {@code false} if entity was not found.
     */
    public boolean deleteImageEntityById(Long id) {
        boolean isDeleted = databaseService.deleteImageEntityById(id);
        if (isDeleted) {
            log.info("Image entity with ID {} deleted successfully.", id);
        } else {
            log.warn("Image entity with ID {} not found for deletion.", id);
        }
        return isDeleted;
    }

    /**
     * Deletes all image entities from the database.
     */
    public void deleteAllImageEntities() {
        databaseService.deleteAllImageEntities();
        log.info("All image entities deleted successfully.");
    }

    /**
     * Imports image data from an XML file.
     *
     * @param file The XML file containing image data to be imported.
     * @return A message indicating success or failure.
     * @throws IOException If an error occurs during file processing.
     */
    public String importImageData(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            log.warn("No file provided for import.");
            return "No file provided.";
        }
        xmlParseService.parseAndSaveXmlData(file);
        log.info("Images imported successfully.");
        return "Image data imported successfully!";
    }
}
