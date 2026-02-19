package net.causw.app.main.domain.community.vote.service.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.domain.community.vote.repository.VoteRepository;

import lombok.RequiredArgsConstructor;

/**
 * Vote 도메인의 쓰기 작업을 담당하는 컴포넌트
 * Repository를 통해 데이터를 생성, 수정, 삭제합니다.
 */
@Component
@RequiredArgsConstructor
@Transactional
public class VoteWriter {
	private final VoteRepository voteRepository;

	/**
	 * Vote를 삭제합니다.
	 *
	 * @param vote 삭제할 Vote
	 */
	public void delete(Vote vote) {
		voteRepository.delete(vote);
	}
}
