package at.bbrz.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

public record HighscoreResponse(@JsonSetter(contentNulls = Nulls.FAIL) long[] highscore) {
}
