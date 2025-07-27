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
import stonenotes.model.User;
import stonenotes.model.Note;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NoteRepositoryTest {
    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User createUser(String email, String password, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

    private Note createNote(String title, String content, User user) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setUser(user);
        return note;
    }

    private Note createAndSaveNote(String title, String content, User user) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setUser(user);
        return testEntityManager.persistAndFlush(note);
    }

    @Test
    void shouldFindNotesByUserIdOrderedByCreatedAtDesc() throws InterruptedException {
        // Given
        User user = createUser("test@example.com", "test_password", "Test", "User");
        testEntityManager.persistAndFlush(user);

        Note note1 = createNote("First Note", "Content 1", user);
        Note note2 = createNote("Second Note", "Content 2", user);

        testEntityManager.persistAndFlush(note1);
        Thread.sleep(10);
        testEntityManager.persistAndFlush(note2);

        // When
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(notes).hasSize(2);
        assertThat(notes.get(0).getTitle()).isEqualTo("Second Note");
        assertThat(notes.get(1).getTitle()).isEqualTo("First Note");
    }

    @Test
    void shouldOnlyReturnNotesForSpecificUser() {
        // Given
        User user1 = createUser("user1@example.com", "test_password", "User", "One");
        User user2 = createUser("user2@example.com", "test_password", "User", "Two");
        testEntityManager.persistAndFlush(user1);
        testEntityManager.persistAndFlush(user2);

        Note user1Note = createNote("User 1 Note", "Content", user1);
        Note user2Note = createNote("User 2 Note", "Content", user2);
        testEntityManager.persistAndFlush(user1Note);
        testEntityManager.persistAndFlush(user2Note);

        // When
        List<Note> user1Notes = noteRepository.findByUserIdOrderByCreatedAtDesc(user1.getId());

        // Then
        assertThat(user1Notes).hasSize(1);
        assertThat(user1Notes.get(0).getTitle()).isEqualTo("User 1 Note");
    }

    @Test
    void shouldFindNoteByIdAndUserId() {
        User user = createUser("test@example.com", "test_password", "Test", "User");
        testEntityManager.persistAndFlush(user);

        Note note = createNote("Test Note", "Content", user);
        testEntityManager.persistAndFlush(note);

        Optional<Note> foundNote = noteRepository.findByIdAndUserId(note.getId(), user.getId());

        assertThat(foundNote).isPresent();
        assertThat(foundNote.get().getTitle()).isEqualTo("Test Note");
    }

    @Test
    void shouldNotFindNoteForDifferentUser() {
        User user1 = createUser("user1@example.com", "password", "User", "One");
        User user2 = createUser("user2@example.com", "password", "User", "Two");
        testEntityManager.persistAndFlush(user1);
        testEntityManager.persistAndFlush(user2);

        Note note = createNote("Test Note", "Content", user1);
        testEntityManager.persistAndFlush(note);

        Optional<Note> foundNote = noteRepository.findByIdAndUserId(note.getId(), user2.getId());

        assertThat(foundNote).isEmpty();
    }

    @Test
    void shouldReturnPaginatedNotesOrderedByCreatedAtDesc() throws InterruptedException {
        User user = new User();
        user.setEmail("user@example.com");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setPassword("test_password");
        user = testEntityManager.persistAndFlush(user);

        Note note1 = createAndSaveNote("Note 1", "Content 1", user);
        Thread.sleep(10);
        Note note2 = createAndSaveNote("Note 2", "Content 2", user);
        Thread.sleep(10);
        Note note3 = createAndSaveNote("Note 3", "Content 3", user);
        Thread.sleep(10);
        Note note4 = createAndSaveNote("Note 4", "Content 4", user);
        Thread.sleep(10);
        Note note5 = createAndSaveNote("Note 5", "Content 5", user);
        Thread.sleep(10);

        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Note> result = noteRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(3);

        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Note 5");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Note 4");
        assertThat(result.getContent().get(2).getTitle()).isEqualTo("Note 3");
    }
}
