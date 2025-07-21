package net.causw.app.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.service.inquiry.InquiryService;
import net.causw.app.main.dto.inquiry.InquiryCreateRequestDto;
import net.causw.app.main.dto.inquiry.InquiryResponseDto;
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
            @AuthenticationPrincipal String requestUserId,
            @PathVariable("id") String id
    ) {
        return this.inquiryService.findById(requestUserId,id);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public InquiryResponseDto create(
            @AuthenticationPrincipal String requestUserId,
            @Valid @RequestBody InquiryCreateRequestDto inquiryCreateRequestDto
    ) {
        return this.inquiryService.create(requestUserId, inquiryCreateRequestDto);
    }
}
