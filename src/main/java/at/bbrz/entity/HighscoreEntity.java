package at.bbrz.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("HIGHSCORE")
public record HighscoreEntity(@Id Long id, @Column("SCORE_ORDER") int position, long score) {
}
