package net.causw.app.main.domain.community.vote.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.domain.community.vote.entity.VoteOption;
import net.causw.app.main.domain.community.vote.entity.VoteRecord;
import net.causw.app.main.domain.community.vote.repository.VoteRecordRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class VoteRecordWriter {
	private final VoteRecordRepository voteRecordRepository;

	public VoteRecord save(VoteRecord voteRecord) {
		return voteRecordRepository.save(voteRecord);
	}

	public void deleteByVoteOptionAndUser(VoteOption voteOption, User user) {
		voteRecordRepository.deleteByVoteOptionAndUser(voteOption, user);
	}

	public void deleteAllByVoteAndUser(Vote vote, User user) {
		voteRecordRepository.deleteAllByVoteOption_VoteAndUser(vote, user);
	}
}
