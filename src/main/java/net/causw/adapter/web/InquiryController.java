package net.causw.adapter.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.application.inquiry.InquiryService;
import net.causw.application.dto.inquiry.InquiryCreateRequestDto;
import net.causw.application.dto.inquiry.InquiryResponseDto;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class InquiryController {
    private final InquiryService inquiryService;

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public InquiryResponseDto findById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") String id
    ) {
        return this.inquiryService.findById(userDetails.getUser(), id);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public InquiryResponseDto create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InquiryCreateRequestDto inquiryCreateRequestDto
    ) {
        return this.inquiryService.create(userDetails.getUser(), inquiryCreateRequestDto);
    }
}
