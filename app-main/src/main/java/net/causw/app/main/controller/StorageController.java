package net.causw.app.main.controller;

import static org.springframework.util.MimeTypeUtils.*;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.model.enums.uuidFile.FilePath;
import net.causw.app.main.dto.file.FileResponseDto;
import net.causw.app.main.service.uuidFile.UuidFileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/storage", produces = APPLICATION_JSON_VALUE)
public class StorageController {

	private final UuidFileService uuidFileService;

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	public FileResponseDto post(
		@RequestPart("file") MultipartFile multipartFile,
		@RequestParam("type") FilePath filePath
	) {
		return FileResponseDto.from(uuidFileService.saveFile(multipartFile, filePath).getFileUrl());
	}

}
