package net.causw.adapter.web;

import lombok.RequiredArgsConstructor;
import net.causw.application.dto.form.FormCreateRequestDto;
import net.causw.application.dto.form.FormResponseDto;
import net.causw.application.form.FormService;
import net.causw.config.security.SecurityService;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/forms")
public class FormController {
    private final FormService formService;

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
    @PreAuthorize("@securityService.activeAndNotNoneUser and @securityService.hasAccessToForm(#formId)")
    public FormResponseDto getForm(@PathVariable String formId) {
        return formService.getForm(formId);
    }


}
