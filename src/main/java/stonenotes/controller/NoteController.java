package stonenotes.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import stonenotes.common.ApiResponse;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.dto.UpdateNoteDto;
import stonenotes.service.NoteService;

@RestController
@RequestMapping("/api")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/notes")
    public ResponseEntity<ApiResponse<NoteResponseDto>> createNote(@Valid @RequestBody CreateNoteDto createNoteDto, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");
        NoteResponseDto noteResponseDto = noteService.createNote(createNoteDto, userId);

        ApiResponse<NoteResponseDto> response = ApiResponse.success(noteResponseDto, "Note created successfully", 201);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/notes")
    public ResponseEntity<ApiResponse<Page<NoteResponseDto>>> getNotes(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String userId = jwt.getClaim("sub");

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<NoteResponseDto> notesPage = noteService.findNotesByUserId(userId, pageable);

        ApiResponse<Page<NoteResponseDto>> response = ApiResponse.success(notesPage, "Notes retrieved successfully", 200);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponseDto>> getNote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long noteId) {
        String userId = jwt.getClaim("sub");

        NoteResponseDto note = noteService.findNoteByIdAndUserId(noteId, userId);

        ApiResponse<NoteResponseDto> response = ApiResponse.success(note, "Note retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponseDto>> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteDto updateNoteDto,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");

        NoteResponseDto updatedNote = noteService.updateNote(noteId, updateNoteDto, userId);

        ApiResponse<NoteResponseDto> response = ApiResponse.success(updatedNote, "Note updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");

        noteService.deleteNote(noteId, userId);

        ApiResponse<Void> response = ApiResponse.success(null, "Note deleted successfully");
        return ResponseEntity.ok(response);
    }
}
