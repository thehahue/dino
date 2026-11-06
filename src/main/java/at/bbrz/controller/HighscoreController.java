package at.bbrz.controller;

import at.bbrz.model.HighscoreResponse;
import at.bbrz.service.HighscoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HighscoreController {

    private final HighscoreService highscoreService;

    public HighscoreController(HighscoreService highscoreService) {
        this.highscoreService = highscoreService;
    }

    @GetMapping("/highscore")
    public HighscoreResponse getHighscore() {
        return highscoreService.getHighscore();
    }

    @PostMapping("/highscore")
    public HighscoreResponse saveHighscore(@RequestBody HighscoreResponse highscore) {
        return highscoreService.saveHighscore(highscore);
    }
}
