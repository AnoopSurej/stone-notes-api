package stonenotes.repository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import stonenotes.builders.NoteBuilder;
import stonenotes.model.Note;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NoteRepositoryTest {
    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Note createNote(String title, String content, String userId) {
        return NoteBuilder.aNote()
                .withTitle(title)
                .withContent(content)
                .withUserId(userId)
                .build();
    }

    private void createAndSaveNote(String title, String content, String userId) {
        Note note = NoteBuilder.aNote()
                .withTitle(title)
                .withContent(content)
                .withUserId(userId)
                .build();
        testEntityManager.persistAndFlush(note);
    }

    @Test
    void shouldFindNotesByUserIdOrderedByCreatedAtDesc() throws InterruptedException {
        // Given
        String userId = "test_user_id";
        Note note1 = createNote("First Note", "Content 1", userId);
        Note note2 = createNote("Second Note", "Content 2", userId);

        testEntityManager.persistAndFlush(note1);
        Thread.sleep(10);
        testEntityManager.persistAndFlush(note2);

        // When
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);

        assertThat(notes).hasSize(2);
        assertThat(notes.get(0).getTitle()).isEqualTo("Second Note");
        assertThat(notes.get(1).getTitle()).isEqualTo("First Note");
    }

    @Test
    void shouldOnlyReturnNotesForSpecificUser() {
        // Given
        String userId_1 = "test_user_id_1";
        String userId_2 = "test_user_id_2";

        Note user1Note = createNote("User 1 Note", "Content", userId_1);
        Note user2Note = createNote("User 2 Note", "Content", userId_2);
        testEntityManager.persistAndFlush(user1Note);
        testEntityManager.persistAndFlush(user2Note);

        // When
        List<Note> user1Notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId_1);

        // Then
        assertThat(user1Notes).hasSize(1);
        assertThat(user1Notes.getFirst().getTitle()).isEqualTo("User 1 Note");
    }

    @Test
    void shouldFindNoteByIdAndUserId() {
        String userId = "test_user_id";

        Note note = createNote("Test Note", "Content", userId);
        testEntityManager.persistAndFlush(note);

        Optional<Note> foundNote = noteRepository.findByIdAndUserId(note.getId(), userId);

        assertThat(foundNote).isPresent();
        assertThat(foundNote.get().getTitle()).isEqualTo("Test Note");
    }

    @Test
    void shouldNotFindNoteForDifferentUser() {
        String userId_1 = "test_user_id_1";
        String userId_2 = "test_user_id_2";

        Note note = createNote("Test Note", "Content", userId_1);
        testEntityManager.persistAndFlush(note);

        Optional<Note> foundNote = noteRepository.findByIdAndUserId(note.getId(), userId_2);

        assertThat(foundNote).isEmpty();
    }

    @Test
    void shouldReturnPaginatedNotesOrderedByCreatedAtDesc() throws InterruptedException {
        String userId = "test_user_id";

        createAndSaveNote("Note 1", "Content 1", userId);
        Thread.sleep(10);
        createAndSaveNote("Note 2", "Content 2", userId);
        Thread.sleep(10);
        createAndSaveNote("Note 3", "Content 3", userId);
        Thread.sleep(10);
        createAndSaveNote("Note 4", "Content 4", userId);
        Thread.sleep(10);
        createAndSaveNote("Note 5", "Content 5", userId);
        Thread.sleep(10);

        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Note> result = noteRepository.findByUserId(userId, pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(3);

        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Note 5");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Note 4");
        assertThat(result.getContent().get(2).getTitle()).isEqualTo("Note 3");
    }

    @Test
    void shouldReturnPaginatedNotesOrderedByCreatedAtAsc() throws InterruptedException {
        String userId = "test_user_id";

        createAndSaveNote("Note 1", "Content 1", userId);
        Thread.sleep(10);
        createAndSaveNote("Note 2", "Content 2", userId);
        Thread.sleep(10);
        createAndSaveNote("Note 3", "Content 3", userId);
        Thread.sleep(10);
        createAndSaveNote("Note 4", "Content 4", userId);
        Thread.sleep(10);
        createAndSaveNote("Note 5", "Content 5", userId);
        Thread.sleep(10);

        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<Note> result = noteRepository.findByUserId(userId, pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(3);

        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Note 1");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Note 2");
        assertThat(result.getContent().get(2).getTitle()).isEqualTo("Note 3");
    }
}
