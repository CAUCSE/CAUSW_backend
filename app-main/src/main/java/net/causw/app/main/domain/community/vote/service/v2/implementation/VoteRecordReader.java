package net.causw.app.main.domain.community.vote.service.v2.implementation;

import java.util.List;
import java.util.Optional;

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
@Transactional(readOnly = true)
public class VoteRecordReader {
	private final VoteRecordRepository voteRecordRepository;

	public Optional<VoteRecord> findByVoteOptionAndUser(VoteOption voteOption, User user) {
		return voteRecordRepository.findByVoteOptionAndUser(voteOption, user);
	}

	public List<VoteRecord> findAllByVote(Vote vote) {
		return voteRecordRepository.findAllByVoteOption_Vote(vote);
	}
}
