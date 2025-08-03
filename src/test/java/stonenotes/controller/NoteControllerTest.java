package stonenotes.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import stonenotes.builders.NoteResponseDtoBuilder;
import stonenotes.common.ApiResponse;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.exception.NoteNotFoundException;
import stonenotes.service.NoteService;
import stonenotes.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private NoteService noteService;

    @InjectMocks
    private NoteController noteController;

    @Test
    void shouldCreateNoteForValidUser() {
        // Given
        String email = "user@example.com";
        Long userId = 1L;
        CreateNoteDto dto = new CreateNoteDto("Test Note", "Test Content");
        LocalDateTime now = LocalDateTime.now();
        NoteResponseDto expectedResponse = NoteResponseDtoBuilder.aNoteResponseDto()
                .withTitle(dto.getTitle())
                .withContent(dto.getContent())
                .withCreatedAt(now)
                .withUpdatedAt(now)
                .build();
        Authentication auth = mock(Authentication.class);

        when(auth.getName()).thenReturn(email);
        when(userService.getUserIdByEmail(email)).thenReturn(userId);
        when(noteService.createNote(dto, userId)).thenReturn(expectedResponse);

        ResponseEntity<ApiResponse<NoteResponseDto>> response = noteController.createNote(dto, auth);
        ApiResponse<NoteResponseDto> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(201, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals(responseBody.getData().getTitle(), "Test Note");
        assertEquals(responseBody.getData().getContent(), "Test Content");
        assertEquals(responseBody.getData().getCreatedAt(), now);
        assertEquals(responseBody.getData().getUpdatedAt(), now);

        verify(userService, times(1)).getUserIdByEmail(email);
        verify(noteService, times(1)).createNote(dto, userId);
    }

    @Test
    void shouldReturnPaginatedNotesSuccessfully() {
        String email = "user@example.com";
        Long userId = 1L;
        Authentication auth = mock(Authentication.class);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

        NoteResponseDto note1 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(1L)
                .withTitle("First Note")
                .withContent("First content")
                .withCreatedAt(LocalDateTime.now().minusHours(2))
                .withUpdatedAt(LocalDateTime.now().minusHours(2))
                .build();
        NoteResponseDto note2 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(2L)
                .withTitle("Second Note")
                .withContent("Second content")
                .withCreatedAt(LocalDateTime.now().minusHours(1))
                .withUpdatedAt(LocalDateTime.now().minusHours(1))
                .build();

        List<NoteResponseDto> noteList = Arrays.asList(note2, note1);
        Page<NoteResponseDto> notePage = new PageImpl<>(noteList, pageable, 5);

        when(auth.getName()).thenReturn(email);
        when(userService.getUserIdByEmail(email)).thenReturn(userId);
        when(noteService.findNotesByUserId(userId, pageable)).thenReturn(notePage);

        ResponseEntity<ApiResponse<Page<NoteResponseDto>>> response = noteController.getNotes(
                auth, 0, 2, "createdAt", "desc"
        );
        ApiResponse<Page<NoteResponseDto>> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals("Notes retrieved successfully", responseBody.getMessage());

        Page<NoteResponseDto> resultPage = responseBody.getData();
        assertEquals(2, resultPage.getContent().size());
        assertEquals(5, resultPage.getTotalElements());
        assertEquals(3, resultPage.getTotalPages());
        assertEquals(0, resultPage.getNumber());
        assertEquals("Second Note", resultPage.getContent().get(0).getTitle());

        verify(userService, times(1)).getUserIdByEmail(email);
        verify(noteService, times(1)).findNotesByUserId(userId, pageable);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Rare case: Should be caught by authentication filter
        // Given
        CreateNoteDto dto = new CreateNoteDto("Title", "Content");
        Authentication auth = mock(Authentication.class);

        when(auth.getName()).thenReturn("nonexistent@example.com");
        when(userService.getUserIdByEmail("nonexistent@example.com")).thenThrow(new UsernameNotFoundException("User not found"));

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> noteController.createNote(dto, auth)
        );

        assertEquals("User not found", ex.getMessage());

        verify(userService, times(1)).getUserIdByEmail("nonexistent@example.com");
        verify(noteService, never()).createNote(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenNoteServiceFails() {
        //Given
        CreateNoteDto dto = new CreateNoteDto("Test Note", "Test Content");
        Authentication auth = mock(Authentication.class);
        String email = "user@example.com";
        Long userId = 1L;

        when(auth.getName()).thenReturn(email);
        when(userService.getUserIdByEmail(email)).thenReturn(userId);
        when(noteService.createNote(dto, userId)).thenThrow(
                new RuntimeException("Service temporarily unavailable")
        );

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> noteController.createNote(dto, auth)
        );

        assertEquals(ex.getMessage(), "Service temporarily unavailable");
        verify(userService, times(1)).getUserIdByEmail(email);
        verify(noteService, times(1)).createNote(dto, userId);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationNameIsNull() {
        // Rare case: Custom authentication implementation returns null
        // Given
        CreateNoteDto dto = new CreateNoteDto("Test Note", "Test Content");
        Authentication auth = mock(Authentication.class);

        when(auth.getName()).thenReturn(null);
        when(userService.getUserIdByEmail(null))
                .thenThrow(new UsernameNotFoundException("User not found"));

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> noteController.createNote(dto, auth)
        );

        assertEquals("User not found", ex.getMessage());
        verify(userService, times(1)).getUserIdByEmail(null);
        verify(noteService, never()).createNote(any(), any());
    }

    @Test
    void shouldReturnNoteSuccessfully() {
        String email = "user@example.com";
        Long userId = 1L;
        Authentication auth = mock(Authentication.class);

        Long noteId = 2L;
        NoteResponseDto dto = NoteResponseDtoBuilder
                .aNoteResponseDto()
                .withId(noteId)
                .withTitle("Note Title")
                .withContent("Note content")
                .build();

        when(auth.getName()).thenReturn(email);
        when(userService.getUserIdByEmail(email)).thenReturn(userId);
        when(noteService.findNoteByIdAndUserId(noteId, userId)).thenReturn(dto);

        ResponseEntity<ApiResponse<NoteResponseDto>> response = noteController.getNote(auth, noteId);
        ApiResponse<NoteResponseDto> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals("Note retrieved successfully", responseBody.getMessage());

        NoteResponseDto data = responseBody.getData();
        assertEquals(data.getId(), noteId);
        assertEquals(data.getTitle(), "Note Title");
        assertEquals(data.getContent(), "Note content");
        assertNotNull(data.getCreatedAt());
        assertNotNull(data.getUpdatedAt());

        verify(userService).getUserIdByEmail(email);
        verify(noteService).findNoteByIdAndUserId(noteId, userId);
    }

    @Test
    void shouldThrowExceptionWhenNoteNotFound() {
        String email = "user@example.com";
        Long userId = 1L;
        Authentication auth = mock(Authentication.class);

        Long noteId = 2L;

        when(auth.getName()).thenReturn(email);
        when(userService.getUserIdByEmail(email)).thenReturn(userId);
        when(noteService.findNoteByIdAndUserId(noteId, userId)).thenThrow(
                new NoteNotFoundException("Note not found")
        );

        assertThatThrownBy(() -> noteController.getNote(auth, noteId))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessage("Note not found");
    }
}
