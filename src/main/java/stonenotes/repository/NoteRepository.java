package stonenotes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stonenotes.model.Note;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Note> findByIdAndUserId(Long id, Long userId);

    Page<Note> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
