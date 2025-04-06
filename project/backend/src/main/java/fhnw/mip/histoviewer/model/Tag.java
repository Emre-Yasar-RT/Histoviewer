package fhnw.mip.histoviewer.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing tags assigned to DICOM images.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Unique tag name

    @JsonBackReference
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "dicomdata_tags",
            joinColumns = {
                    @JoinColumn(name = "tag_id", nullable = false),
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "dicomdata_id", nullable = false)
            }
    )
    private List<DicomData> dicomData;

    public Tag() {
        this.dicomData = new ArrayList<>(); // Initialize the list in the constructor
    }
}
