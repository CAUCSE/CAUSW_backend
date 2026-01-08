package net.causw.app.main.domain.community.board.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.causw.app.main.domain.community.board.entity.FavoriteBoard;

public interface FavoriteBoardRepository extends JpaRepository<FavoriteBoard, String> {
	List<FavoriteBoard> findByUser_Id(String name);
}
