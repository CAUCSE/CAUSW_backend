package net.causw.app.main.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.dto.form.request.FormReplyRequestDto;
import net.causw.app.main.dto.form.response.FormResponseDto;
import net.causw.app.main.dto.form.response.QuestionSummaryResponseDto;
import net.causw.app.main.dto.form.response.reply.ReplyPageResponseDto;
import net.causw.app.main.dto.form.response.reply.UserReplyResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.form.FormService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/forms")
public class FormController {

	private final FormService formService;

	@PutMapping("/{formId}/set-closed")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "신청서 마감 여부 설정", description = "신청서의 마감 여부를 설정합니다.")
	public void setFormIsClosed(
		@PathVariable(name = "formId") String formId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestHeader @NotNull Boolean targetIsClosed
	) {
		formService.setFormIsClosed(formId, userDetails.getUser(), targetIsClosed);
	}

	@GetMapping("/{formId}/can-reply")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "신청서 응답 가능 여부 조회", description = "신청서 응답이 가능한지 여부를 조회합니다.")
	public Boolean getCanReplyToPostForm(
		@PathVariable(name = "formId") String formId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return formService.getCanReplyToPostForm(userDetails.getUser(), formId);
	}

	@GetMapping("/{formId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "신청서 조회", description = "신청서를 조회합니다.")
	public FormResponseDto getForm(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable(name = "formId") String formId
	) {
		return formService.getFormById(userDetails.getUser(), formId);
	}

	@PostMapping("/{formId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "신청서 응답 작성", description = "신청서 응답을 작성합니다.")
	public void replyForm(
		@PathVariable(name = "formId") String formId,
		@Valid @RequestBody FormReplyRequestDto formReplyRequestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		formService.replyForm(formId, formReplyRequestDto, userDetails.getUser());
	}

	@GetMapping("/{formId}/results")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "신청서 결과 전체 페이징 조회", description = "신청서 결과 전체를 페이징으로 조회합니다. 게시글의 신청서는 게시글 작성자만, 동아리 신청서는 동아리장만 조회가 가능합니다.")
	public ReplyPageResponseDto findAllReplyPageByForm(
		@PathVariable(name = "formId") String formId,
		@ParameterObject Pageable pageable,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return formService.findAllReplyPageByForm(formId, pageable, userDetails.getUser());
	}

	@GetMapping("/{formId}/summary")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "신청서 결과 요약 조회", description = "신청서 결과를 요약 조회합니다.")
	public List<QuestionSummaryResponseDto> findSummaryReply(
		@PathVariable(name = "formId") String formId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return formService.findSummaryReply(formId, userDetails.getUser());
	}

	@GetMapping("/{userId}/{circleId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "동아리 신청서 답변 유저별 조회", description = "각 유저의 동아리 신청서에 대한 답변을 조회합니다.")
	@PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
	public List<UserReplyResponseDto> findReplyByUserAndCircle(
		@PathVariable(name = "userId") String userId,
		@PathVariable(name = "circleId") String circleId
	) {
		return formService.getReplyByUserAndCircle(userId, circleId);
	}

	@GetMapping("/{formId}/export")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "신청서 결과 엑셀 다운로드", description = "신청서 결과를 엑셀로 다운로드합니다.")
	public void exportFormResult(
		@PathVariable(name = "formId") String formId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		HttpServletResponse response
	) {
		formService.exportFormResult(formId, userDetails.getUser(), response);
	}
}
