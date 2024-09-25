package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.form.response.reply.ReplyPageResponseDto;
import net.causw.application.dto.form.response.reply.ReplyResponseDto;
import net.causw.application.dto.form.request.FormReplyRequestDto;
import net.causw.application.dto.form.response.QuestionSummaryResponseDto;
import net.causw.application.form.FormService;
import net.causw.config.security.SecurityService;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.util.MessageUtil;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/forms")
public class FormController {
    private final FormService formService;
    private final SecurityService securityService;

    @DeleteMapping("/{formId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "신청서 삭제", description = "신청서를 삭제합니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT', 'LEADER_CIRCLE')")
    public void deleteForm(
            @PathVariable(name = "formId") String formId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        formService.deleteForm(formId, userDetails.getUser());
    }

    @PostMapping("/{formId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "신청서 작성", description = "신청서를 작성합니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    public void replyForm(
            @PathVariable(name = "formId") String formId,
            @Valid @RequestBody FormReplyRequestDto formReplyRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        formService.replyForm(formId, formReplyRequestDto, userDetails.getUser());
    }

    @GetMapping("/{formId}/results")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "신청서 결과 전체 페이징 조회", description = "신청서 결과 전체를 페이징으로 조회합니다. 게시글의 신청서는 게시글 작성자만, 동아리 신청서는 동아리장만 조회가 가능합니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
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
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    public List<QuestionSummaryResponseDto> findSummaryReply(
            @PathVariable(name = "formId") String formId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return formService.findSummaryReply(formId, userDetails.getUser());
    }

    @GetMapping("/{formId}/export")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "신청서 결과 엑셀 다운로드", description = "신청서 결과를 엑셀로 다운로드합니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    public void exportFormResult(
            @PathVariable(name = "formId") String formId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ){
        formService.exportFormResult(formId, userDetails.getUser(), response);
    }
}
