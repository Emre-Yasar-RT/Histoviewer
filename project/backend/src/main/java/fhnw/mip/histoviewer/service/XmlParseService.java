package fhnw.mip.histoviewer.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import fhnw.mip.histoviewer.model.DicomData;
import fhnw.mip.histoviewer.repository.DicomDataRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Log4j2
@RequiredArgsConstructor
@Getter
@Setter
@Service
public class XmlParseService {

    private final String SOPINSTANCEPREFIX = "1.2.826.0.1.3680043.";
    private final DicomDataRepository dicomDataRepository;

    /**
     * Parses XML data from a MultipartFile and saves the relevant data to the database.
     *
     * @param file The MultipartFile containing XML data.
     * @throws IOException If an I/O error occurs while processing the file.
     */
    public void parseAndSaveXmlData(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            log.error("Received empty or null MultipartFile.");
            throw new IllegalArgumentException("File cannot be null or empty.");
        }

        log.info("Starting XML data parsing from MultipartFile.");
        // Delegate the logic to a private method that handles both MultipartFile and File
        parseAndSaveXmlDataInternal(file.getInputStream());
    }

    /**
     * Parses XML data from a File and saves the relevant data to the database.
     *
     * @param file The File containing XML data.
     * @throws IOException If an I/O error occurs while processing the file.
     */
    public void parseAndSaveXmlData(File file) throws IOException {
        if (file == null || !file.exists()) {
            log.error("File does not exist or is null.");
            throw new IllegalArgumentException("File cannot be null or does not exist.");
        }

        log.info("Starting XML data parsing from File: {}", file.getAbsolutePath());
        // Delegate the logic to a private method that handles both MultipartFile and File
        parseAndSaveXmlDataInternal(new FileInputStream(file));
    }

    /**
     * Shared internal method to parse and save XML data. Handles both InputStream sources (MultipartFile and File).
     *
     * @param inputStream The InputStream containing the XML data.
     * @throws IOException If an I/O error occurs while processing the InputStream.
     */
    private void parseAndSaveXmlDataInternal(InputStream inputStream) throws IOException {
        // Create an XmlMapper for Jackson to read XML data into a Java object
        XmlMapper xmlMapper = new XmlMapper();

        log.info("Reading XML data and mapping to XmlDataService object.");
        // Parse the XML data into an XmlDataService object
        XmlDataService histoImages = xmlMapper.readValue(inputStream, XmlDataService.class);

        // Iterate over the images and map them to the DicomData entity, then save to the database
        for (XmlDataService.Image xmlImage : histoImages.getImages()) {
            String uid = SOPINSTANCEPREFIX + (Integer.parseInt(xmlImage.getUid()));
            log.debug("Processing image with UID: {}", uid);

            // Check if a DicomData with the same UID already exists in the database
            DicomData existingDicomData = dicomDataRepository.findByUid(uid);

            if (existingDicomData == null) {
                // If no existing DicomData with the same UID, create a new one
                log.debug("Creating new DicomData entity with UID: {}", uid);

                DicomData dicomData = new DicomData();
                dicomData.setSrc(xmlImage.getSrc());
                dicomData.setDescription(xmlImage.getDescription());
                dicomData.setOriginalURL(xmlImage.getOriginalURL());
                dicomData.setMagnification(xmlImage.getMagnification());
                dicomData.setUid(uid);
                dicomData.setAuthor(xmlImage.getAuthor());

                // Save the new DicomData entity to the database
                dicomDataRepository.save(dicomData);
                log.debug("Successfully saved new DicomData for UID: {}", uid);
            } else {
                // Optionally, log that the entity already exists
                log.debug("DicomData with UID {} already exists.", uid);
            }
        }
    }
}
