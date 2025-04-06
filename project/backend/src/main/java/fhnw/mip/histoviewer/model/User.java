package fhnw.mip.histoviewer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a user entity in the system.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;  // Primary Key

    @Column(nullable = false, unique = true)
    private String username;  // Unique username

    @Column(columnDefinition = "TEXT")
    private String lastSearch;  // Last search made by the user

    @Column(columnDefinition = "TEXT")
    private String lastImage;  // Last image viewed by the user

    @Column(nullable = false)
    private String defaultSliderValue; // User's preferred slider setting

    @Column(nullable = false)
    private String defaultMode; // Preferred mode (dark/light)

    @Column(nullable = false)
    private String defaultLanguage; // Preferred language

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Prevents infinite loops during JSON serialization
    private List<Comment> comments = new ArrayList<>();
}
