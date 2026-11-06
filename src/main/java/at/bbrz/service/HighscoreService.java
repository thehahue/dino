package at.bbrz.service;

import java.util.stream.IntStream;

import at.bbrz.model.HighscoreResponse;
import at.bbrz.entity.HighscoreEntity;
import at.bbrz.repo.HighscoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HighscoreService {

    private final HighscoreRepository repository;

    public HighscoreService(HighscoreRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public HighscoreResponse getHighscore() {
        long[] scores = repository.findAllByOrderByPositionAsc().stream()
                .mapToLong(HighscoreEntity::score)
                .toArray();
        return new HighscoreResponse(scores);
    }

    @Transactional
    public HighscoreResponse saveHighscore(HighscoreResponse highscore) {
        if (highscore.highscore() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "highscore must contain numbers");
        }
        long[] scores = highscore.highscore();
        repository.deleteAll();
        repository.saveAll(IntStream.range(0, scores.length)
                .mapToObj(position -> new HighscoreEntity(null, position, scores[position]))
                .toList());
        return highscore;
    }
}
