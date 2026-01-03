package stonenotes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import stonenotes.builders.NoteBuilder;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.dto.UpdateNoteDto;
import stonenotes.exception.NoteNotFoundException;
import stonenotes.model.Note;
import stonenotes.repository.NoteRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {
    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void shouldCreateNoteSuccessfully() {
        String userId = "test_user_id";
        CreateNoteDto createNoteDto = new CreateNoteDto();
        createNoteDto.setTitle("Test Note");
        createNoteDto.setContent("Test Content");

        Note savedNote = NoteBuilder.aNote()
                .withId(1L)
                .withTitle("Test Note")
                .withContent("Test content")
                .withUserId(userId)
                .build();

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        NoteResponseDto result = noteService.createNote(createNoteDto, userId);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Note");
        assertThat(result.getContent()).isEqualTo("Test content");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void shouldThrowExceptionWhenTitleIsBlank() {
        // Given
        String userId = "test_user_id";
        CreateNoteDto createNoteDto = new CreateNoteDto();
        createNoteDto.setTitle(""); // Blank title
        createNoteDto.setContent("Test content");

        // When/Then
        assertThatThrownBy(() -> noteService.createNote(createNoteDto, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Title cannot be blank");
    }

    @Test
    void shouldReturnUserNotesOrderedByCreatedAtDesc() {
        String userId = "test_user_id";

        Note note1 = NoteBuilder.aNote()
                .withId(1L)
                .withTitle("First Note")
                .withContent("First content")
                .withUserId(userId)
                .withCreatedAt(Instant.now().minusSeconds(7200))
                .withUpdatedAt(Instant.now().minusSeconds(7200))
                .build();

        Note note2 = NoteBuilder.aNote()
                .withId(2L)
                .withTitle("Second Note")
                .withContent("Second content")
                .withUserId(userId)
                .withCreatedAt(Instant.now().minusSeconds(3600))
                .withUpdatedAt(Instant.now().minusSeconds(3600))
                .build();

        List<Note> notes = Arrays.asList(note2, note1);

        when(noteRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(notes);

        List<NoteResponseDto> result = noteService.findNotesByUserId(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Second Note");
        assertThat(result.get(1).getTitle()).isEqualTo("First Note");

        verify(noteRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }
    @Test
    void shouldReturnEmptyListWhenUserHasNoNotes() {
        String userId = "test_user_id";
        List<Note> emptyNotes = List.of();

        when(noteRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(emptyNotes);

        List<NoteResponseDto> result = noteService.findNotesByUserId(userId);

        assertThat(result).isEmpty();
        verify(noteRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }


    @Test
    void shouldReturnPaginatedUserNotesOrderedByCreatedAtDesc() {
        String userId = "test_user_id";
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

        Note note1 = NoteBuilder.aNote()
                .withId(1L)
                .withTitle("First Note")
                .withContent("First content")
                .withUserId(userId)
                .withCreatedAt(Instant.now().minusSeconds(7200))
                .withUpdatedAt(Instant.now().minusSeconds(7200))
                .build();

        Note note2 = NoteBuilder.aNote()
                .withId(2L)
                .withTitle("Second Note")
                .withContent("Second content")
                .withUserId(userId)
                .withCreatedAt(Instant.now().minusSeconds(3600))
                .withUpdatedAt(Instant.now().minusSeconds(3600))
                .build();

        List<Note> noteList = Arrays.asList(note2, note1);
        Page<Note> notePage = new PageImpl<>(noteList, pageable, 5);

        when(noteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(notePage);

        Page<NoteResponseDto> result = noteService.findNotesByUserId(userId, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Second Note");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("First Note");

        verify(noteRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    void shouldReturnSingleNote() {
        String userId = "test_user_id";

        Note note = NoteBuilder.aNote()
                .withId(1L)
                .withTitle("Note Title")
                .withContent("Note content")
                .withUserId(userId)
                .build();

        when(noteRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(note));

        NoteResponseDto result = noteService.findNoteByIdAndUserId(1L, userId);

        assertNotNull(result);
        assertThat(result.getTitle()).isEqualTo("Note Title");
        assertThat(result.getContent()).isEqualTo("Note content");
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(noteRepository).findByIdAndUserId(1L,userId);
    }

    @Test
    void shouldThrowNoteNotFoundExceptionWhenNoteNotFound() {
        String userId = "test_user_id";
        Long noteId = 999L;

        when(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.findNoteByIdAndUserId(noteId, userId))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessage("Note not found");

        verify(noteRepository).findByIdAndUserId(noteId, userId);
    }

    @Test
    void shouldUpdateNoteSuccessfully() {
        String userId = "test_user_id";
        Long noteId = 1L;
        UpdateNoteDto updateDto = new UpdateNoteDto("Updated Title", "Updated content");

        Instant originalTime = Instant.now().minusSeconds(3600);
        Note existingNote = NoteBuilder.aNote()
                .withId(noteId)
                .withTitle("Original Title")
                .withContent("Original content")
                .withUserId(userId)
                .withCreatedAt(originalTime)
                .withUpdatedAt(originalTime)
                .build();

        Note updatedNote = NoteBuilder.aNote()
                .withId(noteId)
                .withTitle("Updated Title")
                .withContent("Updated content")
                .withUserId(userId)
                .withCreatedAt(originalTime)
                .withUpdatedAt(Instant.now())
                .build();

        when(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);

        NoteResponseDto result = noteService.updateNote(noteId, updateDto, userId);

        assertNotNull(result);
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getContent()).isEqualTo("Updated content");
        assertThat(result.getCreatedAt()).isEqualTo(existingNote.getCreatedAt());
        assertThat(result.getUpdatedAt()).isAfter(existingNote.getUpdatedAt());

        verify(noteRepository).findByIdAndUserId(noteId, userId);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentNote() {
        String userId = "test_user_id";
        Long noteId = 999L;
        UpdateNoteDto updateDto = new UpdateNoteDto("Updated Title", "Updated content");

        when(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.updateNote(noteId, updateDto, userId))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessage("Note not found");

        verify(noteRepository).findByIdAndUserId(noteId, userId);
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void shouldDeleteNoteSuccessfully() {
        String userId = "test_user_id";
        Long noteId = 1L;

        Note note = NoteBuilder.aNote()
                .withId(noteId)
                .withTitle("Note to delete")
                .withContent("Content to delete")
                .withUserId(userId)
                .build();

        when(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(Optional.of(note));

        noteService.deleteNote(noteId, userId);

        verify(noteRepository).findByIdAndUserId(noteId, userId);
        verify(noteRepository).delete(note);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentNote() {
        String userId = "test_user_id";
        Long noteId = 999L;

        when(noteRepository.findByIdAndUserId(noteId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.deleteNote(noteId, userId))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessage("Note not found");

        verify(noteRepository).findByIdAndUserId(noteId, userId);
        verify(noteRepository, never()).delete(any(Note.class));
    }
}
