package net.causw.app.main.domain.community.vote.service.v2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.causw.app.main.domain.community.vote.repository.VoteRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.vote.api.v2.dto.request.CreateVoteRequest;
import net.causw.app.main.domain.community.vote.api.v2.dto.response.VoteOptionResponse;
import net.causw.app.main.domain.community.vote.api.v2.dto.response.VoteResponse;
import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.domain.community.vote.entity.VoteOption;
import net.causw.app.main.domain.community.vote.entity.VoteRecord;
import net.causw.app.main.domain.community.vote.repository.VoteOptionRepository;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.StatusPolicy;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {

	private final VoteV2Repository voteV2Repository;
	private final VoteOptionRepository voteOptionRepository;
	private final VoteRecordRepository voteRecordRepository;
	private final PostRepository postRepository;

	@Transactional
	public VoteResponse createVote(CreateVoteRequest request, User user) {
		Post post = postRepository.findById(request.getPostId())
			.orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.POST_NOT_FOUND));

		if (!user.equals(post.getWriter())) {
			throw new BadRequestException(ErrorCode.API_NOT_ALLOWED,
				MessageUtil.VOTE_START_NOT_ACCESSIBLE);
		}

		List<VoteOption> voteOptions = request.getOptions().stream()
			.map(VoteOption::of)
			.collect(Collectors.toList());

		Vote vote = Vote.of(
			request.getTitle(),
			request.getAllowAnonymous(),
			request.getAllowMultiple(),
			voteOptions,
			post);
		Vote savedVote = voteV2Repository.save(vote);
		post.updateVote(savedVote);
		voteOptions.forEach(o -> o.updateVote(savedVote));
		voteOptionRepository.saveAll(voteOptions);

		return buildVoteResponseV2(savedVote, user);
	}

	/**
	 * 옵션 토글.
	 * - 같은 옵션 재클릭 → 취소 (allowMultiple 무관)
	 * - 다른 옵션 + allowMultiple=false → 기존 선택 전부 삭제 후 추가
	 * - 다른 옵션 + allowMultiple=true  → 그냥 추가
	 */
	@Transactional
	public VoteResponse toggleVote(String voteId, String optionId, User user) {
		Vote vote = findVoteOrThrow(voteId);

		if (vote.isEnd()) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.VOTE_ALREADY_END);
		}

		VoteOption voteOption = voteOptionRepository.findById(optionId)
			.orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.VOTE_OPTION_NOT_FOUND));

		if (!voteOption.getVote().getId().equals(voteId)) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER,
				MessageUtil.VOTE_OPTION_NOT_IN_VOTE);
		}

		Optional<VoteRecord> existing = voteRecordRepository.findByVoteOptionAndUser(voteOption, user);

		if (existing.isPresent()) {
			// 같은 옵션 재클릭 → 취소
			voteRecordRepository.deleteByVoteOptionAndUser(voteOption, user);
		} else {
			if (!vote.isAllowMultiple()) {
				// allowMultiple=false: 기존 선택 전부 제거 후 새 선택 추가.
				// SELECT-then-DELETE 동작. 벌크 삭제 아님. 기존 선택은 항상 1건 이하.
				voteRecordRepository.deleteAllByVoteOption_VoteAndUser(vote, user);
			}
			voteRecordRepository.save(VoteRecord.of(user, voteOption));
		}

		return buildVoteResponseV2(vote, user);
	}

	@Transactional
	public VoteResponse endVote(String voteId, User user) {
		Vote vote = findVoteOrThrow(voteId);

		if (!user.equals(vote.getPost().getWriter())) {
			throw new BadRequestException(ErrorCode.API_NOT_ALLOWED, MessageUtil.VOTE_END_NOT_ACCESSIBLE);
		}
		if (vote.isEnd()) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.VOTE_ALREADY_END);
		}

		vote.endVote();
		voteV2Repository.save(vote);
		return buildVoteResponseV2(vote, user);
	}

	@Transactional
	public VoteResponse restartVote(String voteId, User user) {
		Vote vote = findVoteOrThrow(voteId);

		if (!user.equals(vote.getPost().getWriter())) {
			throw new BadRequestException(ErrorCode.API_NOT_ALLOWED,
				MessageUtil.VOTE_RESTART_NOT_ACCESSIBLE);
		}
		if (!vote.isEnd()) {
			throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.VOTE_NOT_END);
		}

		vote.restartVote();
		voteV2Repository.save(vote);
		return buildVoteResponseV2(vote, user);
	}

	@Transactional(readOnly = true)
	public VoteResponse getVoteById(String voteId, User user) {
		return buildVoteResponseV2(findVoteOrThrow(voteId), user);
	}

	@Transactional(readOnly = true)
	public VoteResponse getVoteByPostId(String postId, User user) {
		Vote vote = voteV2Repository.findByPostId(postId)
			.orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.VOTE_NOT_FOUND));
		return buildVoteResponseV2(vote, user);
	}

	// ───────────────────────────── private helpers ─────────────────────────────

	private Vote findVoteOrThrow(String voteId) {
		return voteV2Repository.findById(voteId)
			.orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.VOTE_NOT_FOUND));
	}

	private VoteResponse buildVoteResponseV2(Vote vote, User user) {
		// N+1 방지: 유저의 투표 기록을 한 번에 조회 → votedByMe 계산에 재사용
		Set<String> votedOptionIds = voteRecordRepository
			.findAllByVoteOption_VoteAndUser(vote, user)
			.stream()
			.map(r -> r.getVoteOption().getId())
			.collect(Collectors.toSet());

		List<VoteOption> options = voteOptionRepository.findByVoteId(vote.getId())
			.stream()
			.sorted(Comparator.comparing(VoteOption::getCreatedAt))
			.toList();

		Set<String> uniqueVoterIds = new HashSet<>();
		int totalVoteCount = 0;
		List<VoteOptionResponse> optionResponses = new ArrayList<>();

		for (int i = 0; i < options.size(); i++) {
			VoteOption option = options.get(i);
			List<VoteRecord> records = voteRecordRepository.findAllByVoteOption(option);

			totalVoteCount += records.size();
			records.forEach(r -> uniqueVoterIds.add(r.getUser().getId()));

			// 익명 투표이면 투표자 ID 노출하지 않음
			List<String> voteUsers = vote.isAllowAnonymous()
				? List.of()
				: records.stream().map(r -> r.getUser().getId()).toList();

			optionResponses.add(VoteOptionResponse.of(
				option,
				i,
				records.size(),
				votedOptionIds.contains(option.getId()),
				voteUsers));
		}

		return VoteResponse.of(
			vote,
			StatusPolicy.isVoteOwner(vote, user),
			!votedOptionIds.isEmpty(),
			totalVoteCount,
			uniqueVoterIds.size(),
			optionResponses);
	}
}
