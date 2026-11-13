package at.bbrz.service;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import at.bbrz.model.HighscoreEntry;
import at.bbrz.model.HighscoreResponse;
import at.bbrz.entity.HighscoreEntity;
import at.bbrz.repo.HighscoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HighscoreService {

    private static final Logger log = Logger.getLogger(HighscoreService.class.getName());
    private static final long MAX_SAFE_SCORE = 9_007_199_254_740_991L;
    private final HighscoreRepository repository;

    @Autowired
    public HighscoreService(HighscoreRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public HighscoreResponse getHighscore() {
        log.info("Getting highscore data");
        List<HighscoreEntry> scores = repository.findAllByOrderByPositionAsc().stream()
                .map(row -> new HighscoreEntry(row.playerName(), row.score()))
                .toList();
        return new HighscoreResponse(scores);
    }

    @Transactional
    public HighscoreResponse saveHighscore(HighscoreResponse highscore) {
        if (highscore == null || highscore.highscores() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "highscores must contain entries");
        }
        List<HighscoreEntry> scores = highscore.highscores().stream().map(entry -> {
            if (entry == null || entry.playerName() == null || entry.playerName().isBlank()
                    || entry.playerName().strip().length() > 40) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "playerName must contain 1 to 40 characters");
            }
            if (entry.score() == null || entry.score() < 0 || entry.score() > MAX_SAFE_SCORE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "score must be a non-negative JavaScript-safe integer");
            }
            return new HighscoreEntry(entry.playerName().strip(), entry.score());
        }).toList();
        log.info("Saving " + scores.size() + " highscore entries" + Arrays.toString(scores.toArray()));
        repository.deleteAll();
        repository.saveAll(IntStream.range(0, scores.size())
                .mapToObj(position -> new HighscoreEntity(null, position,
                        scores.get(position).playerName(), scores.get(position).score()))
                .toList());
        return new HighscoreResponse(scores);
    }
}
