package net.causw.app.main.domain.community.vote.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.domain.community.vote.entity.VoteOption;
import net.causw.app.main.domain.community.vote.entity.VoteRecord;
import net.causw.app.main.domain.user.account.entity.user.User;

public interface VoteRecordRepository extends JpaRepository<VoteRecord, String> {
	List<VoteRecord> findAllByVoteOption(VoteOption voteOption);

	List<VoteRecord> findByVoteOption_VoteAndUser(Vote vote, User user);

	boolean existsByVoteOption_VoteAndUser(Vote vote, User user);

	//v2진행시 필요
	Optional<VoteRecord> findByVoteOptionAndUser(VoteOption voteOption, User user);

	/** 특정 옵션 투표 취소 (toggleVote 취소 경로) */
	void deleteByVoteOptionAndUser(VoteOption voteOption, User user);

	/**
	 * 유저의 해당 투표 내 모든 레코드 삭제.
	 * 벌크 삭제 아님 — Spring Data가 SELECT 후 건별 DELETE 실행.
	 * allowMultiple=false 교체 시 기존 선택이 1건이므로 허용.
	 */
	void deleteAllByVoteOption_VoteAndUser(Vote vote, User user);

	/** N+1 방지: votedByMe 계산용 — 유저의 투표 기록 한 번에 조회 */

	/** N+1 방지: 특정 투표의 모든 투표 기록을 한 번에 조회 (옵션별 그룹화 + 사용자 필터 둘 다 메모리에서 처리) */
	@Query("SELECT vr FROM VoteRecord vr " +
		"JOIN FETCH vr.user " +
		"JOIN FETCH vr.voteOption " +
		"WHERE vr.voteOption.vote = :vote") // 재검토
	List<VoteRecord> findAllByVoteOption_Vote(Vote vote);

	List<VoteRecord> findAllByVoteOption_VoteAndUser(Vote vote, User user);

}
