package net.causw.adapter.web;

import lombok.RequiredArgsConstructor;
import net.causw.application.dto.form.FormCreateRequestDto;
import net.causw.application.dto.form.FormResponseDto;
import net.causw.application.form.FormService;
import net.causw.config.security.SecurityService;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/forms")
public class FormController {
    private final FormService formService;
    private final SecurityService securityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FormResponseDto createForm(
            @RequestBody FormCreateRequestDto formCreateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    )
    {
        return formService.createForm(userDetails.getUser(), formCreateRequestDto);
    }

    @GetMapping("/{formId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.activeAndNotNoneUser")
    public FormResponseDto getForm(@PathVariable(name = "formId") String formId) {
        if (!securityService.hasAccessToForm(formId)) {
            throw new UnauthorizedException(ErrorCode.API_NOT_ACCESSIBLE, MessageUtil.API_NOT_ACCESSIBLE);
        }
        return formService.getForm(formId);
    }


}
