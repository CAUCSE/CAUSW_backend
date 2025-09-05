package net.causw.app.main.repository.board;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.model.entity.board.FavoriteBoard;

public interface FavoriteBoardRepository extends JpaRepository<FavoriteBoard, String> {
	List<FavoriteBoard> findByUser_Id(String name);
}
