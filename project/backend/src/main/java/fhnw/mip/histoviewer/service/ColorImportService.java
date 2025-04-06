package fhnw.mip.histoviewer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fhnw.mip.histoviewer.model.DicomData;
import fhnw.mip.histoviewer.repository.DicomDataRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service that handles the import and processing of color data from a JSON file,
 * and updates the color information for DICOM data stored in the repository.
 * <p>
 * The service performs the following tasks:
 * <ul>
 *     <li>Updates the primary color of DICOM data based on the provided JSON file.</li>
 *     <li>Determines the closest color based on HSB (Hue, Saturation, Brightness) values.</li>
 *     <li>Waits for DICOM data entries on startup and updates their color based on a JSON file.</li>
 * </ul>
 * <p>
 * The color determination process involves extracting the hue, saturation, and brightness values
 * from the JSON file, calculating the closest matching color from a predefined set of target colors,
 * and updating the DICOM data with the corresponding primary color.
 * </p>
 * <p>
 * The service also uses asynchronous processing to wait for the DICOM data to be available before
 * performing the update, ensuring the process does not block the application's startup.
 * </p>
 *
 * @see DicomDataRepository
 * @see DicomData
 */
@Log4j2
@Service
public class ColorImportService {

    private final DicomDataRepository dicomDataRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructor to initialize the ColorImportService.
     *
     * @param dicomDataRepository The repository used to access and update DICOM data.
     * @param objectMapper The ObjectMapper used for JSON processing.
     */
    @Autowired
    public ColorImportService(DicomDataRepository dicomDataRepository, ObjectMapper objectMapper) {
        this.dicomDataRepository = dicomDataRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Updates the primary color of DICOM data based on the color information from a JSON file.
     * <p>
     * The JSON file should have entries where the key is the `src` field of the DICOM data, and the value
     * contains the HSB values (`hue`, `saturation`, `brightness`) for color determination.
     * </p>
     *
     * @param jsonFilePath The file path of the JSON file containing color data.
     * @throws IOException If an error occurs while reading the JSON file.
     */
    public void updateColorJson(String jsonFilePath) {
        try {
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
            rootNode.fields().forEachRemaining(entry -> {
                String src = entry.getKey();
                JsonNode colorData = entry.getValue();

                float hue = (float) colorData.get("hue").asDouble();
                float saturation = (float) colorData.get("saturation").asDouble();
                float brightness = (float) colorData.get("brightness").asDouble();

                String primaryColor = determineClosestColor(hue, saturation, brightness);

                DicomData dicomData = dicomDataRepository.findBySrc(src);
                if (dicomData != null) {
                    dicomData.setPrimaryColor(primaryColor);
                    dicomDataRepository.save(dicomData);
                } else {
                    log.warn("DicomData not found for src: {}", src);
                }
            });
        } catch (IOException e) {
            log.error("Error reading the JSON file: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during color update: {}", e.getMessage(), e);
        }
    }

    /**
     * Determines the closest matching color based on the provided HSB values.
     * <p>
     * This method compares the given hue, saturation, and brightness values to a predefined set of target colors.
     * The closest matching color is determined by calculating the distance between the HSB values in the color space.
     * </p>
     *
     * @param hue The hue value (0-360 degrees).
     * @param saturation The saturation value (0-100%).
     * @param brightness The brightness value (0-100%).
     * @return The closest color as a string (e.g., "pink", "blue", "green", "brown", "white").
     */
    private String determineClosestColor(float hue, float saturation, float brightness) {
        if (saturation < 30 || brightness < 30) { // If saturation or brightness is too low, return "white"
            return "white";
        }

        String[] targetColors = {"pink", "blue", "green", "brown"};
        float[][] targetHSB = {
                {300, 70, 80},  // Pink
                {210, 88, 55},  // Blue
                {120, 75, 55},  // Green
                {25, 50, 40},   // Brown
        };

        float minDistance = Float.MAX_VALUE;
        String closestColor = "";
        float hueWeight = 2.0f;

        for (int i = 0; i < targetColors.length; i++) {
            float hueDifference = Math.min(Math.abs(hue - targetHSB[i][0]), 360 - Math.abs(hue - targetHSB[i][0]));
            float satDifference = Math.abs(saturation - targetHSB[i][1]);
            float brightDifference = Math.abs(brightness - targetHSB[i][2]);

            // Calculating the distance in the HSB space
            float distance = (float) Math.sqrt(hueWeight * hueDifference * hueDifference
                    + satDifference * satDifference
                    + brightDifference * brightDifference);

            if (distance < minDistance) {
                minDistance = distance;
                closestColor = targetColors[i];
            }
        }

        return closestColor;
    }

    /**
     * Waits for DICOM data entries to be available and then updates their color based on the provided JSON file.
     * <p>
     * This method runs asynchronously and waits for the DICOM data to be available (non-zero count) before processing
     * the color update.
     * </p>
     *
     * @param path The path to the JSON file containing color data.
     */
    @Async
    public void updateColorOnStartup(String path) {

        Path filePath = Paths.get(path);
        try {
            updateColorJson(filePath.toString());
            log.info("Color update successful.");
        } catch (Exception e) {
            log.error("Error updating color from JSON file: {}", e.getMessage(), e);
        }
    }
}
