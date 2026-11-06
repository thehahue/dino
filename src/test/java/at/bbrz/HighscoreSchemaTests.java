package at.bbrz;

import javax.sql.DataSource;

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
        assertThat(service.getHighscore().highscore()).containsExactly(2000, 1000, 500, 10, 5);
        assertThat(repository.count()).isEqualTo(5);

        service.saveHighscore(new HighscoreResponse(new long[] {3000, 1500}));
        assertThat(repository.findAllByOrderByPositionAsc())
                .allSatisfy(row -> assertThat(row.id()).isGreaterThan(5L));

        ResourceDatabasePopulator initializer =
                new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        initializer.execute(dataSource);
        assertThat(service.getHighscore().highscore()).containsExactly(3000, 1500);

        service.saveHighscore(new HighscoreResponse(new long[0]));
        initializer.execute(dataSource);
        assertThat(repository.count()).isZero();
    }
}
