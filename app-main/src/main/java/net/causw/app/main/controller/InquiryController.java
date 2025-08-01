package net.causw.app.main.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.dto.inquiry.InquiryCreateRequestDto;
import net.causw.app.main.dto.inquiry.InquiryResponseDto;
import net.causw.app.main.service.inquiry.InquiryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
		return this.inquiryService.findById(requestUserId, id);
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
