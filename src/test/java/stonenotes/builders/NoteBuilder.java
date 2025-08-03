package stonenotes.builders;

import stonenotes.model.Note;
import stonenotes.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class NoteBuilder {
    private Long id = null;
    private String title = "Note Title " + UUID.randomUUID().toString().substring(0, 8);
    private String content = "Note content " + UUID.randomUUID().toString().substring(0, 8);
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private User user;

    private NoteBuilder() {}

    public static NoteBuilder aNote() {
        return new NoteBuilder();
    }

    public NoteBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public NoteBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public NoteBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public NoteBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public NoteBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public NoteBuilder withUser(User user) {
        this.user = user;
        return this;
    }

    public Note build() {
        Note note = new Note();
        if (id != null) {
            note.setId(id);
        }
        note.setTitle(title);
        note.setContent(content);
        note.setCreatedAt(createdAt);
        note.setUpdatedAt(updatedAt);
        note.setUser(user);
        return note;
    }
}