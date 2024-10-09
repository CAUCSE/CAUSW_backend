package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.vote.CastVoteRequestDto;
import net.causw.application.dto.vote.CreateVoteRequestDto;
import net.causw.application.dto.vote.VoteResponseDto;
import net.causw.application.vote.VoteService;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/votes")
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "투표 생성", description = "새로운 투표를 생성합니다.")
    public ResponseEntity<VoteResponseDto> createVote(
            @RequestBody CreateVoteRequestDto createVoteRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        VoteResponseDto voteResponse = voteService.createVote(createVoteRequestDto, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(voteResponse);
    }

    @PostMapping("/cast")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "투표 참여", description = "해당 투표에 참여합니다.")
    public ResponseEntity<String> castVote(
            @RequestBody CastVoteRequestDto castVoteRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String result = voteService.castVote(castVoteRequestDto, userDetails.getUser());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{voteId}/end")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "투표 종료", description = "특정 투표를 종료합니다.")
    public ResponseEntity<VoteResponseDto> endVote(
            @PathVariable("voteId") String voteId, // 파라미터 이름 명시
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(voteService.endVote(voteId, userDetails.getUser()));
    }

    @PostMapping("/{voteId}/restart")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "투표 재시작", description = "특정 투표를 재시작합니다.")
    public ResponseEntity<VoteResponseDto> restartVote(
            @PathVariable("voteId") String voteId, // 파라미터 이름 명시
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(voteService.restartVote(voteId, userDetails.getUser()));
    }

    @GetMapping("/{voteId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "투표 조회", description = "특정 투표에 대한 정보를 조회합니다.")
    public ResponseEntity<VoteResponseDto> getVoteById(
            @PathVariable("voteId") String voteId, // 파라미터 이름 명시
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        VoteResponseDto voteResponse = voteService.getVoteById(voteId, userDetails.getUser());
        return ResponseEntity.ok(voteResponse);
    }
}
