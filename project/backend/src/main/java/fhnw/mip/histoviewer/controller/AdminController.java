package fhnw.mip.histoviewer.controller;

import fhnw.mip.histoviewer.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AdminController {

    private final AdminService adminService;

    /**
     * Deletes an image entity by its ID.
     *
     * @param id The ID of the image entity to delete.
     * @return {@code 204 No Content} if deletion is successful, {@code 404 Not Found} otherwise.
     */
    @Operation(summary = "Delete an image entity by its ID",
            description = "Deletes a single image entity from the database using its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the image entity"),
            @ApiResponse(responseCode = "404", description = "Image entity not found")
    })
    @DeleteMapping("/imageEntity/{id}")
    public ResponseEntity<Void> deleteImageEntityById(
            @Parameter(description = "ID of the image entity to delete")
            @PathVariable Long id) {
        boolean isDeleted = adminService.deleteImageEntityById(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Deletes all image entities.
     *
     * @return {@code 204 No Content} if deletion is successful.
     */
    @Operation(summary = "Delete all image entities",
            description = "Deletes all image entities stored in the database.")
    @ApiResponse(responseCode = "204", description = "Successfully deleted all image entities")
    @DeleteMapping("/allImageEntities")
    public ResponseEntity<Void> deleteAllImageEntities() {
        adminService.deleteAllImageEntities();
        return ResponseEntity.noContent().build();
    }

    /**
     * Imports image data from an XML file.
     *
     * @param file The XML file to import.
     * @return {@code 200 OK} with a success message if import is successful,
     *         {@code 400 Bad Request} if no file is provided,
     *         or {@code 500 Internal Server Error} if an error occurs.
     */
    @Operation(summary = "Import image data from XML file",
            description = "Imports image data by parsing an uploaded XML file and saving it to the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image data imported successfully",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Image data imported successfully!"))
            ),
            @ApiResponse(responseCode = "400", description = "No file provided",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "No file provided"))
            ),
            @ApiResponse(responseCode = "500", description = "Error importing images",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Error importing image data: [error_message]"))
            )
    })
    @PostMapping(value = "/XMLData", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> importImageData(
            @Parameter(description = "The XML file to import",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary", example = "sample.xml"))
            )
            @RequestParam("file") MultipartFile file) {
        try {
            String resultMessage = adminService.importImageData(file);
            return ResponseEntity.ok(resultMessage);
        } catch (IOException e) {
            log.error("Error importing image data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error importing images: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during image data import: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }
}
