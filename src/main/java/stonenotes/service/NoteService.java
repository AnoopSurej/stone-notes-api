package stonenotes.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import stonenotes.dto.CreateNoteDto;
import stonenotes.dto.NoteResponseDto;
import stonenotes.dto.UpdateNoteDto;
import stonenotes.exception.NoteNotFoundException;
import stonenotes.model.Note;
import stonenotes.repository.NoteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {
    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public NoteResponseDto createNote(CreateNoteDto createNoteDto, String userId) {
        if(createNoteDto.getTitle() == null || createNoteDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }

        Note note = new Note();
        note.setTitle(createNoteDto.getTitle());
        note.setContent(createNoteDto.getContent());
        note.setUserId(userId);

        Note savedNote = noteRepository.save(note);

        return convertToResponseDto(savedNote);
    }

    public List<NoteResponseDto> findNotesByUserId(String userId) {
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notes.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public Page<NoteResponseDto> findNotesByUserId(String userId, Pageable pageable) {
        Page<Note> notePage = noteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notePage.map(this::convertToResponseDto);
    }

    public NoteResponseDto findNoteByIdAndUserId(Long id, String userId) {
        Note note = noteRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new NoteNotFoundException("Note not found"));

        return convertToResponseDto(note);
    }

    public NoteResponseDto updateNote(Long noteId, UpdateNoteDto updateDto, String userId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found"));

        note.setTitle(updateDto.getTitle());
        note.setContent(updateDto.getContent());

        Note savedNote = noteRepository.save(note);
        return convertToResponseDto(savedNote);
    }

    public void deleteNote(Long noteId, String userId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found"));

        noteRepository.delete(note);
    }

    private NoteResponseDto convertToResponseDto(Note note) {
        NoteResponseDto dto = new NoteResponseDto();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());

        return dto;
    }
}
