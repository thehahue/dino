package at.bbrz.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

public record HighscoreResponse(@JsonSetter(contentNulls = Nulls.FAIL) List<HighscoreEntry> highscores) {
}
