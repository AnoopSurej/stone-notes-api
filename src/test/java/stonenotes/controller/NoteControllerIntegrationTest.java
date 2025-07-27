package stonenotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stonenotes.dto.CreateNoteDto;
import stonenotes.service.NoteService;
import stonenotes.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NoteController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class NoteControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private NoteService noteService;
    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        // Given
        CreateNoteDto dto = new CreateNoteDto("", "Valid Content");
        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnBadRequestWhenTitleTooLong() throws Exception {
        // Given
        String longTitle = "a".repeat(256);
        CreateNoteDto dto = new CreateNoteDto(longTitle, "Valid content");
        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.title").exists());
    }

    @Test
    void shouldReturnBadRequestWhenContentTooLong() throws Exception {
        // Given
        String longContent = "a".repeat(10001);
        CreateNoteDto dto = new CreateNoteDto("Valid title", longContent);
        String jsonRequest = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.content").exists());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsNull() throws Exception {
        String jsonRequest = "{\"title\":null,\"content\":\"Valid content\"}";

        mockMvc.perform(post("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.title").exists());
    }
}
