package net.causw.app.main.domain.moving.repository.board;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.moving.model.entity.board.FavoriteBoard;

public interface FavoriteBoardRepository extends JpaRepository<FavoriteBoard, String> {
	List<FavoriteBoard> findByUser_Id(String name);
}
