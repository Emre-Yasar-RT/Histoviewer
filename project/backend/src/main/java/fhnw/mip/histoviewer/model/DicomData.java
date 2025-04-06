package fhnw.mip.histoviewer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DicomData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // Primary key for the database

    @Column(nullable = false, unique = true)
    private String src; // File name of the image

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description; // Image description

    @Column(nullable = true)
    private String originalURL; // Original URL of the image

    @Column(nullable = true)
    private String magnification; // Magnification level

    @Column(nullable = false, unique = true)
    private String uid; // Unique ID of the image

    @JsonIgnoreProperties // or is @JsonIgnore. Needs more testing.
    @Column(nullable = true)
    private String author; // Author of the image

    @OneToMany(mappedBy = "dicomData", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("dicomData") // Prevents infinite recursion in serialization
    private List<Comment> comments = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "dicom_data_tags",
            joinColumns = @JoinColumn(name = "dicom_data_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties("dicomData") // Prevents infinite recursion in serialization
    @Column(nullable = true)
    private List<Tag> tags = new ArrayList<>();

    @Column(nullable = true)
    private String primaryColor; // Primary color of the image
}
