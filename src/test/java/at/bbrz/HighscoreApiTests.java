package at.bbrz;

import java.util.List;

import at.bbrz.entity.HighscoreEntity;
import at.bbrz.repo.HighscoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
                new HighscoreEntity(1L, 0, "Alex", 2000),
                new HighscoreEntity(2L, 1, "Mia", 1000)));
        mvc.perform(get("/api/highscore"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"highscores":[{"playerName":"Alex","score":2000},{"playerName":"Mia","score":1000}]}
                        """));

        String replacement = """
                {"highscores":[{"playerName":"  Alex  ","score":500},{"playerName":"Mia","score":500},
                {"playerName":"Özlem","score":9007199254740991},{"playerName":"Gast","score":0}]}
                """;
        mvc.perform(post("/api/highscore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(replacement))
                .andExpect(status().isOk())
                .andExpect(content().json(replacement.replace("  Alex  ", "Alex")));
        verify(repository).deleteAll();
        verify(repository).saveAll(List.of(
                new HighscoreEntity(null, 0, "Alex", 500),
                new HighscoreEntity(null, 1, "Mia", 500),
                new HighscoreEntity(null, 2, "Özlem", 9_007_199_254_740_991L),
                new HighscoreEntity(null, 3, "Gast", 0)));

        mvc.perform(post("/api/highscore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"highscores\":[]}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"highscores\":[]}"));
        verify(repository, times(2)).deleteAll();
        verify(repository).saveAll(List.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "null", "{}", "{\"highscores\":null}", "{\"highscores\":[null]}", "{\"highscore\":[500]}",
            "{\"highscores\":[{\"score\":5}]}",
            "{\"highscores\":[{\"playerName\":\"   \",\"score\":5}]}",
            "{\"highscores\":[{\"playerName\":\"12345678901234567890123456789012345678901\",\"score\":5}]}",
            "{\"highscores\":[{\"playerName\":\"Alex\"}]}",
            "{\"highscores\":[{\"playerName\":\"Alex\",\"score\":null}]}",
            "{\"highscores\":[{\"playerName\":\"Alex\",\"score\":-1}]}",
            "{\"highscores\":[{\"playerName\":\"Alex\",\"score\":9007199254740992}]}",
            "{\"highscores\":[{\"playerName\":\"Alex\",\"score\":\"invalid\"}]}",
            "{\"highscores\":[{\"playerName\":\"Alex\",\"score\":5},null]}"
    })
    void rejectsInvalidDataBeforeChangingStoredScores(String json) throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        mvc.perform(post("/api/highscore").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}
