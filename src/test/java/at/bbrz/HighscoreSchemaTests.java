package at.bbrz;

import java.util.List;
import javax.sql.DataSource;

import at.bbrz.model.HighscoreEntry;
import at.bbrz.model.HighscoreResponse;
import at.bbrz.repo.HighscoreRepository;
import at.bbrz.service.HighscoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:highscore-schema-test")
class HighscoreSchemaTests {

    @Autowired
    private HighscoreService service;

    @Autowired
    private HighscoreRepository repository;

    @Autowired
    private DataSource dataSource;

    @Test
    void createsDefaultRowsAndPreservesSavedValuesWhenRunAgain() {
        assertThat(service.getHighscore().highscores()).containsExactly(
                new HighscoreEntry("Unbekannt", 2000L),
                new HighscoreEntry("Unbekannt", 1000L),
                new HighscoreEntry("Unbekannt", 500L),
                new HighscoreEntry("Unbekannt", 10L),
                new HighscoreEntry("Unbekannt", 5L));
        assertThat(repository.count()).isEqualTo(5);

        List<HighscoreEntry> entries = List.of(new HighscoreEntry("Alex", 3000L), new HighscoreEntry("Mia", 1500L));
        service.saveHighscore(new HighscoreResponse(entries));
        assertThat(repository.findAllByOrderByPositionAsc())
                .allSatisfy(row -> assertThat(row.id()).isGreaterThan(5L));

        ResourceDatabasePopulator initializer =
                new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        initializer.execute(dataSource);
        assertThat(service.getHighscore().highscores()).containsExactlyElementsOf(entries);

        service.saveHighscore(new HighscoreResponse(List.of()));
        initializer.execute(dataSource);
        assertThat(repository.count()).isZero();
    }
}
