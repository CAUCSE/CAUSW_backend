package net.causw.app.main.domain.community.board.service.implementation;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.repository.BoardQueryRepository;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.community.board.service.dto.request.BoardQueryCondition;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.errorcode.BoardConfigErrorCode;
import net.causw.app.main.shared.exception.errorcode.BoardErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardReader {
	private final BoardQueryRepository boardQueryRepository;
	private final BoardRepository boardRepository;
	private final BoardConfigReader boardConfigReader;


	public List<Board> findAllByIdsNotDeleted(List<String> boardIds) {
		return boardQueryRepository.findAllByIdsNotDeleted(boardIds);
	}

	/**
	 * 게시판 검색 조회
	 * @param boardQueryCondition 게시판 조회 조건 DTO
	 * @return 게시판 Entity 목록
	 */
	public List<Board> searchBoardList(BoardQueryCondition boardQueryCondition) {

		return boardQueryRepository.findWithConditionOrderByDisplayOrder(boardQueryCondition);
	}

	/**
	 * 게시판 ID로 게시판 조회
	 * @param boardId 게시판 ID
	 * @return 게시판 Entity
	 */
	public Board getById(String boardId) {

		return boardQueryRepository.findById(boardId)
			.orElseThrow(BoardErrorCode.BOARD_NOT_FOUND::toBaseException);
	}

	/**
	 * 게시판 이름으로 존재 여부 조회
	 * @param name 게시판 이름
	 * @return 존재 여부
	 */
	public boolean existsByName(String name) {
		return Boolean.TRUE.equals(boardRepository.existsByName(name));
	}

	/**
	 * 게시판 이름으로, 특정 게시판 ID를 제외한 존재 여부 조회
	 * @param name 게시판 이름
	 * @param excludeBoardId 제외할 게시판 ID
	 * @return 존재 여부
	 */
	public boolean existsByNameExcludingId(String name, String excludeBoardId) {
		return Boolean.TRUE.equals(boardRepository.existsByNameAndIdNot(name, excludeBoardId));
	}

	/**
	 * 사용자의 학적 상태에 따른 읽기 범위 리스트 반환
	 *
	 * @param academicStatus 사용자의 학적 상태
	 * @return 사용자의 학적 상태에 따른 읽기 범위 리스트
	 */
	public List<BoardReadScope> getReadeScopesByAcademicStatus(AcademicStatus academicStatus) {
		if (academicStatus == null) {
			return List.of(BoardReadScope.BOTH);
		}
		return switch (academicStatus) {
			case ENROLLED, LEAVE_OF_ABSENCE, SUSPEND, PROFESSOR ->
				List.of(BoardReadScope.BOTH, BoardReadScope.ENROLLED);
			case GRADUATED -> List.of(BoardReadScope.BOTH, BoardReadScope.GRADUATED);
			default -> List.of(BoardReadScope.BOTH);
		};
	}

	/**
	 * 유저의 학적 상태에 맞는 삭제되지 않은 공지사항 게시판 목록을 반환한다.
	 *
	 * @param user 조회 대상 유저
	 * @return 접근 가능한 공지사항 게시판 목록
	 */
	public List<Board> findAccessibleNoticeBoards(User user) {
		List<BoardReadScope> readScopes = getReadeScopesByAcademicStatus(user.getAcademicStatus());
		List<String> boardIds = boardConfigReader.findAllNoticeConfigsByReadScope(new HashSet<>(readScopes))
			.stream()
			.map(BoardConfig::getBoardId)
			.toList();
		if (boardIds.isEmpty()) {
			return List.of();
		}
		return findAllByIdsNotDeleted(boardIds);
	}

	/**
	 * 게시판 ID가 공지사항 게시판인지 검증하고 반환한다.
	 *
	 * @param boardId 게시판 ID
	 * @return 공지사항 게시판 Entity
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 공지사항 게시판이 아닌 경우
	 */
	public Board getNoticeBoardOrThrow(String boardId) {
		if (!boardConfigReader.existsByBoardIdAndIsNotice(boardId)) {
			throw BoardConfigErrorCode.BOARD_NOT_NOTICE.toBaseException();
		}
		return getById(boardId);
	}
}
