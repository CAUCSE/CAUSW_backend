package net.causw.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteBoardRepository extends JpaRepository<FavoriteBoard, String> {
    List<FavoriteBoard> findByUser_Id(String name);

    List<FavoriteBoard> findByBoard_Id(String name);
}
