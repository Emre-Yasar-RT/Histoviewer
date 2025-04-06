package fhnw.mip.histoviewer.repository;

import fhnw.mip.histoviewer.model.DicomData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing Dicomdata in the database.
 */
@Repository
public interface DicomDataRepository extends JpaRepository<DicomData, Long>, JpaSpecificationExecutor<DicomData> {

    /**
     * Finds a DicomData entity by its UID.
     *
     * @param uid the unique identifier of the DicomData
     * @return an Optional containing the DicomData, or an empty Optional if not found
     */
    DicomData findByUid(String uid);

    /**
     * Finds a DicomData entity by its source.
     *
     * @param src the source string associated with the DicomData
     * @return the DicomData entity, or null if not found
     */
    DicomData findBySrc(String src);

}
