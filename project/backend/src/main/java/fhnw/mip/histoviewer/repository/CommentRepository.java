package fhnw.mip.histoviewer.repository;

import fhnw.mip.histoviewer.model.Comment;
import fhnw.mip.histoviewer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository interface for accessing Comments in the database.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Deletes all comments associated with a specific user.
     *
     * @param user the user whose comments are to be deleted
     */
    @Modifying
    @Transactional
    void deleteAllCommentsByUser(User user);
}
