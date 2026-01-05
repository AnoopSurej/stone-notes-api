package stonenotes.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import stonenotes.builders.NoteResponseDtoBuilder;
import stonenotes.common.ApiResponse;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.dto.UpdateNoteDto;
import stonenotes.exception.NoteNotFoundException;
import stonenotes.service.NoteService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteControllerTest {
    @Mock
    private NoteService noteService;

    @InjectMocks
    private NoteController noteController;

    @Test
    void shouldCreateNoteForValidUser() {
        // Given
        String userId = "keycloak-user-uuid-123";
        CreateNoteDto dto = new CreateNoteDto("Test Note", "Test Content");
        Instant now = Instant.now();
        NoteResponseDto expectedResponse = NoteResponseDtoBuilder.aNoteResponseDto()
                .withTitle(dto.getTitle())
                .withContent(dto.getContent())
                .withCreatedAt(now)
                .withUpdatedAt(now)
                .build();
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.createNote(dto, userId)).thenReturn(expectedResponse);

        ResponseEntity<ApiResponse<NoteResponseDto>> response = noteController.createNote(dto, jwt);
        ApiResponse<NoteResponseDto> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(201, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals("Test Note", responseBody.getData().getTitle());
        assertEquals("Test Content", responseBody.getData().getContent());
        assertEquals(now, responseBody.getData().getCreatedAt());
        assertEquals(now, responseBody.getData().getUpdatedAt());

        verify(jwt, times(1)).getClaim("sub");
        verify(noteService, times(1)).createNote(dto, userId);
    }

    @Test
    void shouldReturnPaginatedNotesSuccessfully() {
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

        NoteResponseDto note1 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(1L)
                .withTitle("First Note")
                .withContent("First content")
                .withCreatedAt(Instant.now().minusSeconds(7200))
                .withUpdatedAt(Instant.now().minusSeconds(7200))
                .build();
        NoteResponseDto note2 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(2L)
                .withTitle("Second Note")
                .withContent("Second content")
                .withCreatedAt(Instant.now().minusSeconds(3600))
                .withUpdatedAt(Instant.now().minusSeconds(3600))
                .build();

        List<NoteResponseDto> noteList = Arrays.asList(note2, note1);
        Page<NoteResponseDto> notePage = new PageImpl<>(noteList, pageable, 5);

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.findNotesByUserId(userId, pageable)).thenReturn(notePage);

        ResponseEntity<ApiResponse<Page<NoteResponseDto>>> response = noteController.getNotes(
                jwt, 0, 2, "createdAt", "desc"
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

        verify(jwt, times(1)).getClaim("sub");
        verify(noteService, times(1)).findNotesByUserId(userId, pageable);
    }

    @Test
    void shouldReturnNotesOrderedByCreatedAtAsc() {
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "createdAt"));

        NoteResponseDto note1 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(1L)
                .withTitle("First Note")
                .withContent("First content")
                .withCreatedAt(Instant.now().minusSeconds(7200))
                .withUpdatedAt(Instant.now().minusSeconds(7200))
                .build();
        NoteResponseDto note2 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(2L)
                .withTitle("Second Note")
                .withContent("Second content")
                .withCreatedAt(Instant.now().minusSeconds(3600))
                .withUpdatedAt(Instant.now().minusSeconds(3600))
                .build();

        List<NoteResponseDto> noteList = Arrays.asList(note1, note2);
        Page<NoteResponseDto> notePage = new PageImpl<>(noteList, pageable, 2);

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.findNotesByUserId(userId, pageable)).thenReturn(notePage);

        ResponseEntity<ApiResponse<Page<NoteResponseDto>>> response = noteController.getNotes(
                jwt, 0, 2, "createdAt", "asc"
        );
        ApiResponse<Page<NoteResponseDto>> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());

        Page<NoteResponseDto> resultPage = responseBody.getData();
        assertEquals(2, resultPage.getContent().size());
        assertEquals("First Note", resultPage.getContent().get(0).getTitle());
        assertEquals("Second Note", resultPage.getContent().get(1).getTitle());

        verify(jwt, times(1)).getClaim("sub");
        verify(noteService, times(1)).findNotesByUserId(userId, pageable);
    }

    @Test
    void shouldReturnNotesOrderedByTitleAsc() {
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "title"));

        NoteResponseDto note1 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(1L)
                .withTitle("A Note")
                .build();
        NoteResponseDto note2 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(2L)
                .withTitle("B Note")
                .build();

        List<NoteResponseDto> noteList = Arrays.asList(note1, note2);
        Page<NoteResponseDto> notePage = new PageImpl<>(noteList, pageable, 2);

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.findNotesByUserId(userId, pageable)).thenReturn(notePage);

        ResponseEntity<ApiResponse<Page<NoteResponseDto>>> response = noteController.getNotes(
                jwt, 0, 2, "title", "asc"
        );
        ApiResponse<Page<NoteResponseDto>> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());

        Page<NoteResponseDto> resultPage = responseBody.getData();
        assertEquals(2, resultPage.getContent().size());
        assertEquals("A Note", resultPage.getContent().get(0).getTitle());
        assertEquals("B Note", resultPage.getContent().get(1).getTitle());

        verify(jwt, times(1)).getClaim("sub");
        verify(noteService, times(1)).findNotesByUserId(userId, pageable);
    }

    @Test
    void shouldReturnNotesOrderedByTitleDesc() {
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "title"));

        NoteResponseDto note1 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(1L)
                .withTitle("Z Note")
                .build();
        NoteResponseDto note2 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(2L)
                .withTitle("Y Note")
                .build();

        List<NoteResponseDto> noteList = Arrays.asList(note1, note2);
        Page<NoteResponseDto> notePage = new PageImpl<>(noteList, pageable, 2);

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.findNotesByUserId(userId, pageable)).thenReturn(notePage);

        ResponseEntity<ApiResponse<Page<NoteResponseDto>>> response = noteController.getNotes(
                jwt, 0, 2, "title", "desc"
        );
        ApiResponse<Page<NoteResponseDto>> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());

        Page<NoteResponseDto> resultPage = responseBody.getData();
        assertEquals(2, resultPage.getContent().size());
        assertEquals("Z Note", resultPage.getContent().get(0).getTitle());
        assertEquals("Y Note", resultPage.getContent().get(1).getTitle());

        verify(jwt, times(1)).getClaim("sub");
        verify(noteService, times(1)).findNotesByUserId(userId, pageable);
    }

    @Test
    void shouldReturnNotesOrderedByUpdatedAtAsc() {
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "updatedAt"));

        NoteResponseDto note1 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(1L)
                .withTitle("Older Update")
                .withContent("Content 1")
                .withCreatedAt(Instant.now().minusSeconds(7200))
                .withUpdatedAt(Instant.now().minusSeconds(3600))
                .build();
        NoteResponseDto note2 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(2L)
                .withTitle("Recent Update")
                .withContent("Content 2")
                .withCreatedAt(Instant.now().minusSeconds(7200))
                .withUpdatedAt(Instant.now().minusSeconds(1800))
                .build();

        List<NoteResponseDto> noteList = Arrays.asList(note1, note2);
        Page<NoteResponseDto> notePage = new PageImpl<>(noteList, pageable, 2);

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.findNotesByUserId(userId, pageable)).thenReturn(notePage);

        ResponseEntity<ApiResponse<Page<NoteResponseDto>>> response = noteController.getNotes(
                jwt, 0, 2, "updatedAt", "asc"
        );
        ApiResponse<Page<NoteResponseDto>> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());

        Page<NoteResponseDto> resultPage = responseBody.getData();
        assertEquals(2, resultPage.getContent().size());
        assertEquals("Older Update", resultPage.getContent().get(0).getTitle());
        assertEquals("Recent Update", resultPage.getContent().get(1).getTitle());

        verify(jwt, times(1)).getClaim("sub");
        verify(noteService, times(1)).findNotesByUserId(userId, pageable);
    }

    @Test
    void shouldReturnNotesOrderedByUpdatedAtDesc() {
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "updatedAt"));

        NoteResponseDto note1 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(1L)
                .withTitle("Recent Update")
                .withContent("Content 1")
                .withCreatedAt(Instant.now().minusSeconds(7200))
                .withUpdatedAt(Instant.now().minusSeconds(1800))
                .build();
        NoteResponseDto note2 = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(2L)
                .withTitle("Old Update")
                .withContent("Content 2")
                .withCreatedAt(Instant.now().minusSeconds(7200))
                .withUpdatedAt(Instant.now().minusSeconds(3600))
                .build();

        List<NoteResponseDto> noteList = Arrays.asList(note1, note2);
        Page<NoteResponseDto> notePage = new PageImpl<>(noteList, pageable, 2);

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.findNotesByUserId(userId, pageable)).thenReturn(notePage);

        ResponseEntity<ApiResponse<Page<NoteResponseDto>>> response = noteController.getNotes(
                jwt, 0, 2, "updatedAt", "desc"
        );
        ApiResponse<Page<NoteResponseDto>> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());

        Page<NoteResponseDto> resultPage = responseBody.getData();
        assertEquals(2, resultPage.getContent().size());
        assertEquals("Recent Update", resultPage.getContent().get(0).getTitle());
        assertEquals("Old Update", resultPage.getContent().get(1).getTitle());

        verify(jwt, times(1)).getClaim("sub");
        verify(noteService, times(1)).findNotesByUserId(userId, pageable);
    }

    @Test
    void shouldThrowExceptionWhenSortByNotInAllowedSortFields() {
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn(userId);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> noteController.getNotes(jwt, 0, 2, "notInAllowedSortFields", "desc")
        );

        assertEquals("Invalid sort field: notInAllowedSortFields", ex.getMessage());
        verify(jwt, times(1)).getClaim("sub");
    }

    @Test
    void shouldThrowExceptionWhenNoteServiceFails() {
        // Given
        CreateNoteDto dto = new CreateNoteDto("Test Note", "Test Content");
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.createNote(dto, userId)).thenThrow(
                new RuntimeException("Service temporarily unavailable")
        );

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> noteController.createNote(dto, jwt)
        );

        assertEquals("Service temporarily unavailable", ex.getMessage());
        verify(jwt, times(1)).getClaim("sub");
        verify(noteService, times(1)).createNote(dto, userId);
    }

    @Test
    void shouldThrowExceptionWhenJwtClaimIsNull() {
        // Edge case: JWT sub claim is null
        // Given
        CreateNoteDto dto = new CreateNoteDto("Test Note", "Test Content");
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("sub")).thenReturn(null);
        when(noteService.createNote(dto, null)).thenThrow(
                new RuntimeException("User ID cannot be null")
        );

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> noteController.createNote(dto, jwt)
        );

        assertEquals("User ID cannot be null", ex.getMessage());
        verify(jwt, times(1)).getClaim("sub");
        verify(noteService, times(1)).createNote(dto, null);
    }

    @Test
    void shouldReturnNoteSuccessfully() {
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);

        Long noteId = 2L;
        NoteResponseDto dto = NoteResponseDtoBuilder
                .aNoteResponseDto()
                .withId(noteId)
                .withTitle("Note Title")
                .withContent("Note content")
                .build();

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.findNoteByIdAndUserId(noteId, userId)).thenReturn(dto);

        ResponseEntity<ApiResponse<NoteResponseDto>> response = noteController.getNote(jwt, noteId);
        ApiResponse<NoteResponseDto> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals("Note retrieved successfully", responseBody.getMessage());

        NoteResponseDto data = responseBody.getData();
        assertEquals(noteId, data.getId());
        assertEquals("Note Title", data.getTitle());
        assertEquals("Note content", data.getContent());
        assertNotNull(data.getCreatedAt());
        assertNotNull(data.getUpdatedAt());

        verify(jwt).getClaim("sub");
        verify(noteService).findNoteByIdAndUserId(noteId, userId);
    }

    @Test
    void shouldThrowExceptionWhenNoteNotFound() {
        String userId = "keycloak-user-uuid-123";
        Jwt jwt = mock(Jwt.class);

        Long noteId = 2L;

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.findNoteByIdAndUserId(noteId, userId)).thenThrow(
                new NoteNotFoundException("Note not found")
        );

        assertThatThrownBy(() -> noteController.getNote(jwt, noteId))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessage("Note not found");
    }

    @Test
    void shouldUpdateNoteSuccessfully() {
        String userId = "keycloak-user-uuid-123";
        Long noteId = 1L;
        Jwt jwt = mock(Jwt.class);
        UpdateNoteDto updateDto = new UpdateNoteDto("Updated Title", "Updated Content");

        NoteResponseDto updatedNote = NoteResponseDtoBuilder.aNoteResponseDto()
                .withId(noteId)
                .withTitle("Updated Title")
                .withContent("Updated Content")
                .build();

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.updateNote(noteId, updateDto, userId)).thenReturn(updatedNote);

        ResponseEntity<ApiResponse<NoteResponseDto>> response = noteController.updateNote(noteId, updateDto, jwt);
        ApiResponse<NoteResponseDto> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals("Note updated successfully", responseBody.getMessage());

        NoteResponseDto data = responseBody.getData();
        assertEquals(noteId, data.getId());
        assertEquals("Updated Title", data.getTitle());
        assertEquals("Updated Content", data.getContent());

        verify(jwt).getClaim("sub");
        verify(noteService).updateNote(noteId, updateDto, userId);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentNote() {
        String userId = "keycloak-user-uuid-123";
        Long noteId = 999L;
        Jwt jwt = mock(Jwt.class);
        UpdateNoteDto updateDto = new UpdateNoteDto("Updated Title", "Updated Content");

        when(jwt.getClaim("sub")).thenReturn(userId);
        when(noteService.updateNote(noteId, updateDto, userId)).thenThrow(
                new NoteNotFoundException("Note not found")
        );

        assertThatThrownBy(() -> noteController.updateNote(noteId, updateDto, jwt))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessage("Note not found");

        verify(jwt).getClaim("sub");
        verify(noteService).updateNote(noteId, updateDto, userId);
    }

    @Test
    void shouldDeleteNoteSuccessfully() {
        String userId = "keycloak-user-uuid-123";
        Long noteId = 1L;
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("sub")).thenReturn(userId);

        ResponseEntity<ApiResponse<Void>> response = noteController.deleteNote(noteId, jwt);
        ApiResponse<Void> responseBody = response.getBody();

        assertNotNull(responseBody);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(responseBody.isSuccess());
        assertEquals("Note deleted successfully", responseBody.getMessage());
        assertNull(responseBody.getData());

        verify(jwt).getClaim("sub");
        verify(noteService).deleteNote(noteId, userId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentNote() {
        String userId = "keycloak-user-uuid-123";
        Long noteId = 999L;
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("sub")).thenReturn(userId);
        doThrow(new NoteNotFoundException("Note not found"))
                .when(noteService).deleteNote(noteId, userId);

        assertThatThrownBy(() -> noteController.deleteNote(noteId, jwt))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessage("Note not found");

        verify(jwt).getClaim("sub");
        verify(noteService).deleteNote(noteId, userId);
    }
}
