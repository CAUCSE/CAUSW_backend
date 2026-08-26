package net.causw.app.main.domain.asset.file.api.v2.controller;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.core.aop.annotation.RequireAdminRole;
import net.causw.app.main.core.aop.enums.AdminTarget;
import net.causw.app.main.domain.asset.file.api.v2.dto.request.MultiplePresignedUrlRequest;
import net.causw.app.main.domain.asset.file.api.v2.dto.request.PresignedUrlRequest;
import net.causw.app.main.domain.asset.file.api.v2.dto.response.FileInfoResponse;
import net.causw.app.main.domain.asset.file.api.v2.dto.response.FileUploadResponse;
import net.causw.app.main.domain.asset.file.api.v2.dto.response.MultipleFilesUploadResponse;
import net.causw.app.main.domain.asset.file.api.v2.dto.response.MultiplePresignedUrlResponse;
import net.causw.app.main.domain.asset.file.api.v2.dto.response.PresignedUrlResponse;
import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.UuidFileService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "File Public v2", description = "파일 관리 API V2")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v2/storage", produces = APPLICATION_JSON_VALUE)
public class FileController {

	private final UuidFileService uuidFileService;

	@Operation(summary = "Presigned URL 발급 (단일)", description = """
		클라이언트가 S3에 직접 업로드할 수 있는 Presigned PUT URL을 발급합니다.
		1. 이 API로 presignedUrl과 uuid를 받습니다.
		2. presignedUrl에 PUT 요청으로 파일을 직접 업로드합니다.
		3. 도메인 생성 API 요청 시 uuid를 전달합니다.
		""")
	@PostMapping(value = "/presigned-url", produces = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<PresignedUrlResponse> issuePresignedUrl(
		@RequestBody @Valid PresignedUrlRequest request) {
		return ApiResponse.success(uuidFileService.issuePresignedUrl(request));
	}

	@Operation(summary = "Presigned URL 발급 (다중)", description = """
		여러 파일을 S3에 직접 업로드하기 위한 Presigned PUT URL 목록을 발급합니다.
		응답의 files 목록은 요청 files 목록과 동일한 순서로 반환됩니다.
		""")
	@PostMapping(value = "/presigned-url/multiple", produces = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<MultiplePresignedUrlResponse> issueMultiplePresignedUrls(
		@RequestBody @Valid MultiplePresignedUrlRequest request) {
		return ApiResponse.success(uuidFileService.issueMultiplePresignedUrls(request));
	}

	@Operation(summary = "파일 업로드 (ADMIN)", description = "단일 파일을 업로드합니다.")
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
	@RequireAdminRole(target = AdminTarget.ALL_ADMIN)
	public ApiResponse<FileUploadResponse> uploadFile(
		@RequestParam("file") MultipartFile file,
		@RequestParam("type") FilePath filePath) {
		log.info("File upload requested. FilePath: {}", filePath);

		UuidFile savedFile = uuidFileService.saveFile(file, filePath);

		return ApiResponse.success(FileUploadResponse.from(savedFile));
	}

	@Operation(summary = "다중 파일 업로드", description = "여러 파일을 한 번에 업로드합니다.")
	@PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
	@RequireAdminRole(target = AdminTarget.ALL_ADMIN)
	public ApiResponse<MultipleFilesUploadResponse> uploadMultipleFiles(
		@RequestParam("files") List<MultipartFile> files,
		@RequestParam("type") FilePath filePath) {
		log.info("Multiple files upload requested. Count: {}, FilePath: {}", files.size(), filePath);

		List<UuidFile> savedFiles = uuidFileService.saveFileList(files, filePath);

		return ApiResponse.success(MultipleFilesUploadResponse.from(savedFiles));
	}

	@Operation(summary = "파일 조회", description = "파일 ID로 파일 정보를 조회합니다.")
	@GetMapping("/{fileId}")
	@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
	@RequireAdminRole(target = AdminTarget.ALL_ADMIN)
	public ApiResponse<FileInfoResponse> getFile(@PathVariable String fileId) {
		log.info("File info requested. FileId: {}", fileId);

		UuidFile file = uuidFileService.findUuidFileById(fileId);

		return ApiResponse.success(FileInfoResponse.from(file));
	}

	@Operation(summary = "파일 수정", description = "기존 파일을 삭제하고 새 파일로 교체합니다.")
	@PutMapping(value = "/{fileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
	@RequireAdminRole(target = AdminTarget.ALL_ADMIN)
	public ApiResponse<FileUploadResponse> updateFile(
		@PathVariable String fileId,
		@RequestParam("file") MultipartFile file,
		@RequestParam("type") FilePath filePath) {
		log.info("File update requested. FileId: {}, FilePath: {}", fileId, filePath);

		UuidFile updatedFile = uuidFileService.updateFile(fileId, file, filePath);

		return ApiResponse.success(FileUploadResponse.from(updatedFile));
	}

	@Operation(summary = "파일 삭제", description = "파일을 삭제합니다.")
	@DeleteMapping("/{fileId}")
	@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
	@RequireAdminRole(target = AdminTarget.ALL_ADMIN)
	public ApiResponse<Void> deleteFile(@PathVariable String fileId) {
		log.info("File delete requested. FileId: {}", fileId);

		uuidFileService.deleteFile(fileId);

		return ApiResponse.success();
	}
}
