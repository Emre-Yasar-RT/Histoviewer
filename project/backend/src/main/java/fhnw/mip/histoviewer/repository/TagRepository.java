package fhnw.mip.histoviewer.repository;

import fhnw.mip.histoviewer.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for accessing Tags in the database.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Finds a list of tags by their name.
     *
     * @param name the name of the tag to search for
     * @return a List containing the list of tags with the given name, or an empty Optional if none found
     */
    List<Tag> findByName(String name);
}

