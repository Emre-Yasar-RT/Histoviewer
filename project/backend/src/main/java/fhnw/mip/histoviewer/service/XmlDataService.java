package fhnw.mip.histoviewer.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * Service for parsing and saving XML data into the database.
 */
@Log4j2
@RequiredArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class XmlDataService {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("image")
    private List<Image> images;

    /**
     * Represents an image with metadata that will be parsed from the XML data.
     * This is an inner class of the {@link XmlDataService} class and is used
     * for mapping image-related data from the XML to a Java object.
     */
    public static class Image {

        private String src;
        private String description;
        private String originalURL;
        private String magnification;
        private String uid;
        @JsonIgnoreProperties
        private String author;

        /**
         * Gets the source of the image.
         *
         * @return The source of the image as a string.
         */
        @JsonProperty
        public String getSrc() {
            return src;
        }

        /**
         * Gets the description of the image.
         *
         * @return The description of the image as a string.
         */
        @JsonProperty
        public String getDescription() {
            return description;
        }

        /**
         * Gets the original URL of the image.
         *
         * @return The original URL of the image as a string.
         */
        @JsonProperty
        public String getOriginalURL() {
            return originalURL;
        }

        /**
         * Gets the magnification value of the image.
         *
         * @return The magnification of the image as a string.
         */
        @JsonProperty
        public String getMagnification() {
            return magnification;
        }

        /**
         * Gets the UID of the image, which is a unique identifier.
         *
         * @return The UID of the image as a string.
         */
        @JsonProperty
        public String getUid() {
            return uid;
        }

        /**
         * Gets the author of the image.
         * This property is ignored during JSON deserialization, as indicated by {@link JsonIgnoreProperties}.
         *
         * @return The author of the image as a string.
         */
        @JsonProperty
        public String getAuthor() {
            return author;
        }
    }
}