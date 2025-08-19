package net.causw.app.main.repository.board;

import net.causw.app.main.domain.model.entity.board.FavoriteBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteBoardRepository extends JpaRepository<FavoriteBoard, String> {
    List<FavoriteBoard> findByUser_Id(String name);
}
