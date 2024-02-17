package net.causw.adapter.web;

import net.causw.application.inquiry.InquiryService;
import net.causw.application.dto.inquiry.InquiryCreateRequestDto;
import net.causw.application.dto.inquiry.InquiryResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public InquiryResponseDto findById(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id
    ) {
        return this.inquiryService.findById(requestUserId,id);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public InquiryResponseDto create(
            @AuthenticationPrincipal String requestUserId,
            @RequestBody InquiryCreateRequestDto inquiryCreateRequestDto
    ) {
        return this.inquiryService.create(requestUserId, inquiryCreateRequestDto);
    }
}
