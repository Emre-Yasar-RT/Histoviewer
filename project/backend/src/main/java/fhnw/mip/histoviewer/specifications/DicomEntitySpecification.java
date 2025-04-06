package fhnw.mip.histoviewer.specifications;

import fhnw.mip.histoviewer.model.Comment;
import fhnw.mip.histoviewer.model.DicomData;
import fhnw.mip.histoviewer.model.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification class for DicomData entity to build dynamic queries based on various search conditions.
 * Inspired by https://www.youtube.com/watch?v=AWBSWlM0JmQ
 */
@Log4j2
public class DicomEntitySpecification {

    /**
     * Creates a specification to search for a term in the description field.
     *
     * @param providedTerm The term to search for in the description.
     * @return A Specification that matches DicomData where the description contains the provided term.
     */
    public static Specification<DicomData> containsTermInDescription(String providedTerm) {
        log.debug("Creating specification for term '{}' in description.", providedTerm);

        return (root, query, criteriaBuilder) -> {
            if (providedTerm == null || providedTerm.isEmpty()) {
                log.warn("Provided term for description search is null or empty.");
                return criteriaBuilder.conjunction();  // Return an empty query if the term is null or empty
            }
            // Ensure query is not null before calling distinct
            if (query != null) {
                query.distinct(true); // Ensure duplicate results are not included
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" +
                    providedTerm.toLowerCase() + "%");
        };
    }


    /**
     * Creates a specification to search for an image that has a specific tag.
     *
     * @param providedTag The tag to search for.
     * @return A Specification that matches DicomData with the specified tag.
     */
    public static Specification<DicomData> hasTag(String providedTag) {
        log.debug("Creating specification for tag '{}' in DicomData.", providedTag);

        return (root, query, criteriaBuilder) -> {
            if (providedTag == null || providedTag.isEmpty()) {
                log.warn("Provided tag for DicomData search is null or empty.");
                return criteriaBuilder.conjunction();  // Return an empty query if the tag is null or empty
            }
            Join<DicomData, Tag> tagJoin = root.join("tags", JoinType.INNER);
            return criteriaBuilder.equal(tagJoin.get("name"), providedTag);
        };
    }

    /**
     * Creates a specification to search for a term in the comments of the DicomData.
     *
     * @param providedTerm The term to search for in the comments.
     * @return A Specification that matches DicomData with comments containing the provided term.
     */
    public static Specification<DicomData> containsTermInComments(String providedTerm) {
        log.debug("Creating specification for term '{}' in comments.", providedTerm);

        return (root, query, criteriaBuilder) -> {
            if (providedTerm == null || providedTerm.isEmpty()) {
                log.warn("Provided term for comment search is null or empty.");
                return criteriaBuilder.conjunction();  // Return an empty query if the term is null or empty
            }
            Join<DicomData, Comment> commentJoin = root.join("comments", JoinType.LEFT);
            return criteriaBuilder.like(criteriaBuilder.lower(commentJoin.get("text")),
                    "%" + providedTerm.toLowerCase() + "%");
        };
    }

    /**
     * Creates a specification to search for a specific color in the DicomData.
     *
     * @param providedColor The color to search for.
     * @return A Specification that matches DicomData with the specified color.
     */
    public static Specification<DicomData> hasColor(String providedColor) {
        log.debug("Creating specification for color '{}' in DicomData.", providedColor);

        return (root, query, criteriaBuilder) -> {
            if (providedColor == null || providedColor.isEmpty()) {
                log.warn("Provided color for DicomData search is null or empty.");
                return criteriaBuilder.conjunction();  // Return an empty query if the color is null or empty
            }
            // Ensure query is not null before calling distinct
            if (query != null) {
                query.distinct(true); // Ensure duplicate results are not included
            }
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("primaryColor")),
                    providedColor.toLowerCase());
        };
    }
}