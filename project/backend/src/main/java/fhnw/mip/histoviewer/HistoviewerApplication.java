package fhnw.mip.histoviewer;

import fhnw.mip.histoviewer.repository.DicomDataRepository;
import fhnw.mip.histoviewer.service.ColorImportService;
import fhnw.mip.histoviewer.service.DatabaseService;
import fhnw.mip.histoviewer.service.XmlParseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;

/**
 * Main application class for the HistoViewer application.
 * This class initializes and starts the Spring Boot application.
 */
@Log4j2
@RequiredArgsConstructor
@SpringBootApplication
public class HistoviewerApplication {

	private final ColorImportService colorImportService;
	private final XmlParseService xmlParseService;

	@Value("${dicom.xml.filepath}")
	private String xmlFilePath;
	@Value("${dicom.json.filepath}")
	private String jsonFilePath;

	/**
	 * Main method to start the Spring Boot application.
	 *
	 * @param args Command-line arguments
	 */
	public static void main(String[] args) {
		log.info("Starting HistoViewer Application...");
		SpringApplication.run(HistoviewerApplication.class, args);
		log.info("HistoViewer Application started successfully.");
	}

	/**
	 * Bean to import DICOM data from an XML file and update color mappings from a JSON file on startup.
	 *
	 * @param dicomDataRepository Repository for accessing DICOM data.
	 * @return CommandLineRunner instance to execute import tasks on application startup.
	 */
	@Bean
	public CommandLineRunner importDicomDataFromXML(DicomDataRepository dicomDataRepository) {
		return args -> {
			log.info("Initializing DICOM data import process...");
			// Define the path to the XML file
			File xmlFile = new File(xmlFilePath);
			File jsonFile = new File(jsonFilePath);

			// Check if the file exists
			if (xmlFile.exists() && xmlFile.isFile()) {
				log.info("Found XML file at '{}'. Starting import...", xmlFilePath);
				try {
					// If the file exists, import the data
					xmlParseService.parseAndSaveXmlData(xmlFile);
					log.info("XML data imported successfully.");
				} catch (IOException e) {
					// Handle the exception if something goes wrong
					log.info("Error importing XML data: {}", e.getMessage());
				}
			} else {
				// If the file doesn't exist, do nothing
				log.info("XML file not found, nothing to import.");
			}
			if (jsonFile.exists() && jsonFile.isFile()) {
				log.info("Found JSON file at '{}'. Updating color mappings...", jsonFilePath);
                // If the file exists, import the data
                colorImportService.updateColorOnStartup(jsonFilePath);
                log.info("Json color data imported successfully.");
            } else {
				// If the file doesn't exist, do nothing
				log.info("XML file not found, nothing to import.");
			}

		};
	}
}
