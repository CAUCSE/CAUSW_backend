package net.causw.app.main.service.board;

import org.springframework.stereotype.Service;

import net.causw.app.main.repository.board.FavoriteBoardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteBoardService {

	private final FavoriteBoardRepository favoriteBoardRepository;

	public boolean isFavorite(String userId, String boardId) {
		return favoriteBoardRepository.findByUser_Id(userId)
			.stream()
			.filter(favoriteBoard -> !favoriteBoard.getBoard().getIsDeleted())
			.anyMatch(favoriteboard -> favoriteboard.getBoard().getId().equals(boardId));
	}
}
