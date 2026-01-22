package net.causw.app.main.domain.asset.file.api.v2.controller;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.api.v2.dto.response.FileInfoResponse;
import net.causw.app.main.domain.asset.file.api.v2.dto.response.FileUploadResponse;
import net.causw.app.main.domain.asset.file.api.v2.dto.response.MultipleFilesUploadResponse;
import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "File V2", description = "파일 관리 API V2")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v2/storage", produces = APPLICATION_JSON_VALUE)
public class FileController {

	private final UuidFileService uuidFileService;

	@Operation(summary = "파일 업로드", description = "단일 파일을 업로드합니다.")
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	public ResponseEntity<FileUploadResponse> uploadFile(
		@RequestParam("file") MultipartFile file,
		@RequestParam("type") FilePath filePath) {
		log.info("File upload requested. FilePath: {}", filePath);

		UuidFile savedFile = uuidFileService.saveFile(file, filePath);

		return ResponseEntity.ok(FileUploadResponse.from(savedFile));
	}

	@Operation(summary = "다중 파일 업로드", description = "여러 파일을 한 번에 업로드합니다.")
	@PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	public ResponseEntity<MultipleFilesUploadResponse> uploadMultipleFiles(
		@RequestParam("files") List<MultipartFile> files,
		@RequestParam("type") FilePath filePath) {
		log.info("Multiple files upload requested. Count: {}, FilePath: {}", files.size(), filePath);

		List<UuidFile> savedFiles = uuidFileService.saveFileList(files, filePath);

		return ResponseEntity.ok(MultipleFilesUploadResponse.from(savedFiles));
	}

	@Operation(summary = "파일 조회", description = "파일 ID로 파일 정보를 조회합니다.")
	@GetMapping("/{fileId}")
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	public ResponseEntity<FileInfoResponse> getFile(@PathVariable String fileId) {
		log.info("File info requested. FileId: {}", fileId);

		UuidFile file = uuidFileService.findUuidFileById(fileId);

		return ResponseEntity.ok(FileInfoResponse.from(file));
	}

	@Operation(summary = "파일 수정", description = "기존 파일을 삭제하고 새 파일로 교체합니다.")
	@PutMapping(value = "/{fileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	public ResponseEntity<FileUploadResponse> updateFile(
		@PathVariable String fileId,
		@RequestParam("file") MultipartFile file,
		@RequestParam("type") FilePath filePath) {
		log.info("File update requested. FileId: {}, FilePath: {}", fileId, filePath);

		UuidFile existingFile = uuidFileService.findUuidFileById(fileId);
		UuidFile updatedFile = uuidFileService.updateFile(existingFile, file, filePath);

		return ResponseEntity.ok(FileUploadResponse.from(updatedFile));
	}

	@Operation(summary = "파일 삭제", description = "파일을 삭제합니다.")
	@DeleteMapping("/{fileId}")
	@PreAuthorize("@security.hasRole(@Role.ADMIN)")
	public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
		log.info("File delete requested. FileId: {}", fileId);

		UuidFile file = uuidFileService.findUuidFileById(fileId);
		uuidFileService.deleteFile(file);

		return ResponseEntity.noContent().build();
	}
}
