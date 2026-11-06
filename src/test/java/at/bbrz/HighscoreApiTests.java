package at.bbrz;

import java.util.List;

import at.bbrz.entity.HighscoreEntity;
import at.bbrz.repo.HighscoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:highscore-api-test")
class HighscoreApiTests {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private HighscoreRepository repository;

    @Test
    void readsValuesAndPassesIndividualRowsToRepository() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(repository.findAllByOrderByPositionAsc()).thenReturn(List.of(
                new HighscoreEntity(1L, 0, 2000),
                new HighscoreEntity(2L, 1, 1000),
                new HighscoreEntity(3L, 2, 500),
                new HighscoreEntity(4L, 3, 10),
                new HighscoreEntity(5L, 4, 5)));
        mvc.perform(get("/api/highscore"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"highscore\":[2000,1000,500,10,5]}"));

        String replacement = "{\"highscore\":[500,500,9223372036854775807,0]}";
        mvc.perform(post("/api/highscore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(replacement))
                .andExpect(status().isOk())
                .andExpect(content().json(replacement));
        verify(repository).deleteAll();
        verify(repository).saveAll(List.of(
                new HighscoreEntity(null, 0, 500),
                new HighscoreEntity(null, 1, 500),
                new HighscoreEntity(null, 2, Long.MAX_VALUE),
                new HighscoreEntity(null, 3, 0)));

        mvc.perform(post("/api/highscore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"highscore\":[]}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"highscore\":[]}"));
        verify(repository, times(2)).deleteAll();
        verify(repository).saveAll(List.of());

        clearInvocations(repository);
        mvc.perform(post("/api/highscore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/highscore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"highscore\":[null]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}
