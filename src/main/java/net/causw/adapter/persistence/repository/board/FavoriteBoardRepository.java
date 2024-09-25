package net.causw.adapter.persistence.repository.board;

import net.causw.adapter.persistence.board.FavoriteBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteBoardRepository extends JpaRepository<FavoriteBoard, String> {
    List<FavoriteBoard> findByUser_Id(String name);
}
