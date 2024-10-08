package net.causw.application.vote;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.vote.VoteOptionRepository;
import net.causw.adapter.persistence.repository.vote.VoteRecordRepository;
import net.causw.adapter.persistence.repository.vote.VoteRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.vote.Vote;
import net.causw.adapter.persistence.vote.VoteOption;
import net.causw.adapter.persistence.vote.VoteRecord;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.util.StatusUtil;
import net.causw.application.dto.util.dtoMapper.UserDtoMapper;
import net.causw.application.dto.util.dtoMapper.VoteDtoMapper;
import net.causw.application.dto.vote.CastVoteRequestDto;
import net.causw.application.dto.vote.CreateVoteRequestDto;
import net.causw.application.dto.vote.VoteOptionResponseDto;
import net.causw.application.dto.vote.VoteResponseDto;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@MeasureTime
@Service
@RequiredArgsConstructor
public class VoteService {
    private final VoteOptionRepository voteOptionRepository;
    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final VoteRecordRepository voteRecordRepository;

    @Transactional
    public VoteResponseDto createVote(CreateVoteRequestDto createVoteRequestDTO, User user) {
        Post post = postRepository.findById(createVoteRequestDTO.getPostId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.POST_NOT_FOUND));
        if (!(user.equals(post.getWriter()))){
            throw new BadRequestException(ErrorCode.API_NOT_ALLOWED, MessageUtil.VOTE_START_NOT_ACCESSIBLE);
        }
        List<VoteOption> voteOptions = createVoteRequestDTO.getOptions().stream()
                .map(VoteOption::of)
                .collect(Collectors.toList());
        Vote vote = Vote.of(
                createVoteRequestDTO.getTitle(),
                createVoteRequestDTO.getAllowAnonymous(),
                createVoteRequestDTO.getAllowMultiple(),
                voteOptions,
                post
        );
        Vote savedVote = voteRepository.save(vote);
        post.updateVote(vote);
        voteOptions.forEach(voteOption -> voteOption.updateVote(savedVote));
        voteOptionRepository.saveAll(voteOptions);
        return toVoteResponseDto(savedVote,user);
    }

    @Transactional
    public String castVote(CastVoteRequestDto castVoteRequestDto, User user) {
        List<String> voteOptionIds = castVoteRequestDto.getVoteOptionIdList();
        if (voteOptionIds == null || voteOptionIds.isEmpty()) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.VOTE_OPTION_NOT_PROVIDED);
        }
        // 첫 번째 VoteOption을 이용해 Vote를 가져옴 (모든 옵션은 동일한 Vote에 속해야 함)
        VoteOption firstVoteOption = voteOptionRepository.findById(castVoteRequestDto.getVoteOptionIdList().get(0))
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.VOTE_OPTION_NOT_FOUND));
        Vote vote = firstVoteOption.getVote();

        // 중복 투표가 허용되지 않는 경우
        if (!vote.isAllowMultiple()) {
            if (voteOptionIds.size() > 1) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.VOTE_NOT_MULTIPLE);
            }
            List<VoteRecord> existingVoteRecords = voteRecordRepository.findByVoteOption_VoteAndUser(vote, user);
            if (!existingVoteRecords.isEmpty()) {
                throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.VOTE_ALREADY_DONE);
            }
        }
        List<VoteOption> voteOptions = voteOptionRepository.findAllById(voteOptionIds);
        List<VoteRecord> newVoteRecords = voteOptions.stream()
                .map(voteOption -> VoteRecord.of(user, voteOption))
                .collect(Collectors.toList());
        voteRecordRepository.saveAll(newVoteRecords);
        return "투표 성공";
    }

    @Transactional
    public VoteResponseDto endVote(String voteId, User user) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST,MessageUtil.VOTE_NOT_FOUND));
        if (!(user.equals(vote.getPost().getWriter()))){
            throw new BadRequestException(ErrorCode.API_NOT_ALLOWED, MessageUtil.VOTE_END_NOT_ACCESSIBLE);
        }
        if (vote.isEnd()) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.VOTE_ALREADY_END);
        }
        vote.endVote();
        voteRepository.save(vote);
        return toVoteResponseDto(vote,user);

    }

    @Transactional
    public VoteResponseDto restartVote(String voteId, User user) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST,MessageUtil.VOTE_NOT_FOUND));
        if (!(user.equals(vote.getPost().getWriter()))){
            throw new BadRequestException(ErrorCode.API_NOT_ALLOWED, MessageUtil.VOTE_END_NOT_ACCESSIBLE);
        }
        if (!vote.isEnd()) {
            throw new BadRequestException(ErrorCode.INVALID_PARAMETER, MessageUtil.VOTE_ALREADY_END);
        }
        vote.restartVote();
        voteRepository.save(vote);
        return toVoteResponseDto(vote,user);
    }

    @Transactional(readOnly = true)
    public VoteResponseDto getVoteById(String voteId, User user) {
        Vote vote = voteRepository.findById(voteId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.VOTE_NOT_FOUND));
        return toVoteResponseDto(vote,user);
    }

    private VoteResponseDto toVoteResponseDto(Vote vote, User user) {
        List<VoteOptionResponseDto> voteOptionResponseDtoList = vote.getVoteOptions().stream()
                .sorted(Comparator.comparing(VoteOption::getCreatedAt))
                .map(this::tovoteOptionResponseDto)
                .collect(Collectors.toList());

        Set<String> uniqueUserIds = voteOptionResponseDtoList.stream()
                .flatMap(voteOptionResponseDto -> voteOptionResponseDto.getVoteUsers().stream())
                .map(UserResponseDto::getId)
                .collect(Collectors.toSet());
        Integer totalUserCount = uniqueUserIds.size();
        return VoteDtoMapper.INSTANCE.toVoteResponseDto(
                vote,
                voteOptionResponseDtoList
                , StatusUtil.isVoteOwner(vote, user)
                , vote.isEnd()
                , voteRecordRepository.existsByVoteOption_VoteAndUser(vote, user)
                , voteOptionResponseDtoList.stream()
                        .mapToInt(VoteOptionResponseDto::getVoteCount)
                        .sum()
                , totalUserCount);
    }

    private VoteOptionResponseDto tovoteOptionResponseDto(VoteOption voteOption) {
        List<VoteRecord> voteRecords = voteRecordRepository.findAllByVoteOption(voteOption);
        List<UserResponseDto> userResponseDtos = voteRecords.stream()
                .map(voteRecord -> UserDtoMapper.INSTANCE.toUserResponseDto(voteRecord.getUser(), null, null))
                .collect(Collectors.toList());
        return VoteDtoMapper.INSTANCE.toVoteOptionResponseDto(voteOption, voteRecords.size(), userResponseDtos);
    }

}
