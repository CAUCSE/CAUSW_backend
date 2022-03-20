package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteBoardRepository extends JpaRepository<FavoriteBoard, String> {
    Optional<FavoriteBoard> findByUser_IdAndBoard_Id(String userId, String boardId);

    List<FavoriteBoard> findByUser_Id(String name);
}
