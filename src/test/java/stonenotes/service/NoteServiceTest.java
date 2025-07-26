package stonenotes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.model.Note;
import stonenotes.model.User;
import stonenotes.repository.NoteRepository;
import stonenotes.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {
    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void shouldCreateNoteSuccessfully() {
        Long userId = 1L;
        CreateNoteDto createNoteDto = new CreateNoteDto();
        createNoteDto.setTitle("Test Note");
        createNoteDto.setContent("Test Content");

        User user = new User();
        user.setEmail("test@example.com");

        Note savedNote = new Note();
        savedNote.setId(1L);
        savedNote.setTitle("Test Note");
        savedNote.setContent("Test content");
        savedNote.setUser(user);
        savedNote.setCreatedAt(LocalDateTime.now());
        savedNote.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        NoteResponseDto result = noteService.createNote(createNoteDto, userId);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Note");
        assertThat(result.getContent()).isEqualTo("Test content");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(userRepository).findById(userId);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        Long userId = 999L;
        CreateNoteDto createNoteDto = new CreateNoteDto();
        createNoteDto.setTitle("Test Note");
        createNoteDto.setContent("Test content");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> noteService.createNote(createNoteDto, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenTitleIsBlank() {
        // Given
        Long userId = 1L;
        CreateNoteDto createNoteDto = new CreateNoteDto();
        createNoteDto.setTitle(""); // Blank title
        createNoteDto.setContent("Test content");

        // When/Then
        assertThatThrownBy(() -> noteService.createNote(createNoteDto, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title cannot be blank");
    }
}
