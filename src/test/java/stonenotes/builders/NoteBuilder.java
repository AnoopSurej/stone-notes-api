package stonenotes.builders;

import stonenotes.model.Note;

import java.time.Instant;
import java.util.UUID;

public class NoteBuilder {
    private Long id = null;
    private String title = "Note Title " + UUID.randomUUID().toString().substring(0, 8);
    private String content = "Note content " + UUID.randomUUID().toString().substring(0, 8);
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private String userId;

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

    public NoteBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public NoteBuilder withUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public NoteBuilder withUserId(String userId) {
        this.userId = userId;
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
        note.setUserId(userId);
        return note;
    }
}