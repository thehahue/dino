package at.bbrz.repo;

import java.util.List;

import at.bbrz.entity.HighscoreEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface HighscoreRepository extends ListCrudRepository<HighscoreEntity, Long> {

    List<HighscoreEntity> findAllByOrderByPositionAsc();
}
