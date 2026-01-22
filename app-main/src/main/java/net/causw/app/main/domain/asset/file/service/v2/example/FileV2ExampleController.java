package net.causw.app.main.domain.asset.file.service.v2.example;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.UuidFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * UuidFileServiceV2 사용 예제 컨트롤러
 * 실제 운영 환경에서는 제거하거나 주석 처리할 것
 */
@Slf4j
@Tag(name = "File V2 Example", description = "파일 서비스 V2 사용 예제 API")
@RestController
@RequestMapping("/api/v2/files/example")
@RequiredArgsConstructor
public class FileV2ExampleController {

	private final UuidFileService fileServiceV2;

	@Operation(summary = "단일 파일 업로드", description = "단일 파일을 업로드합니다.")
	@PostMapping("/upload")
	public ResponseEntity<UuidFile> uploadFile(
		@RequestParam("file") MultipartFile file,
		@RequestParam("filePath") FilePath filePath) {
		log.info("Single file upload requested. FilePath: {}", filePath);

		UuidFile savedFile = fileServiceV2.saveFile(file, filePath);

		return ResponseEntity.ok(savedFile);
	}

	@Operation(summary = "다중 파일 업로드", description = "여러 파일을 한 번에 업로드합니다.")
	@PostMapping("/upload/multiple")
	public ResponseEntity<List<UuidFile>> uploadMultipleFiles(
		@RequestParam("files") List<MultipartFile> files,
		@RequestParam("filePath") FilePath filePath) {
		log.info("Multiple files upload requested. Count: {}, FilePath: {}", files.size(), filePath);

		List<UuidFile> savedFiles = fileServiceV2.saveFileList(files, filePath);

		return ResponseEntity.ok(savedFiles);
	}

	@Operation(summary = "파일 수정", description = "기존 파일을 삭제하고 새 파일로 교체합니다.")
	@PutMapping("/{fileId}")
	public ResponseEntity<UuidFile> updateFile(
		@PathVariable String fileId,
		@RequestParam("file") MultipartFile file,
		@RequestParam("filePath") FilePath filePath) {
		log.info("File update requested. FileId: {}, FilePath: {}", fileId, filePath);

		// 기존 파일 조회
		UuidFile existingFile = fileServiceV2.findUuidFileById(fileId);

		// 파일 수정
		UuidFile updatedFile = fileServiceV2.updateFile(existingFile, file, filePath);

		return ResponseEntity.ok(updatedFile);
	}

	@Operation(summary = "파일 삭제", description = "파일을 삭제합니다.")
	@DeleteMapping("/{fileId}")
	public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
		log.info("File delete requested. FileId: {}", fileId);

		// 파일 조회
		UuidFile file = fileServiceV2.findUuidFileById(fileId);

		// 파일 삭제
		fileServiceV2.deleteFile(file);

		return ResponseEntity.noContent().build();
	}

	/**
	 * 프로필 이미지 업로드 예제
	 */
	@Operation(summary = "프로필 이미지 업로드 예제")
	@PostMapping("/profile-image")
	public ResponseEntity<UuidFile> uploadProfileImage(@RequestParam("image") MultipartFile image) {
		log.info("Profile image upload requested");

		// FilePath.USER_PROFILE은 5MB, 1개, 이미지만 허용
		UuidFile savedFile = fileServiceV2.saveFile(image, FilePath.USER_PROFILE);

		return ResponseEntity.ok(savedFile);
	}

	/**
	 * 게시글 첨부파일 업로드 예제
	 */
	@Operation(summary = "게시글 첨부파일 업로드 예제")
	@PostMapping("/post-attachments")
	public ResponseEntity<List<UuidFile>> uploadPostAttachments(
		@RequestParam("files") List<MultipartFile> files) {
		log.info("Post attachments upload requested. Count: {}", files.size());

		// FilePath.POST는 10MB, 20개, 다양한 확장자 허용
		List<UuidFile> savedFiles = fileServiceV2.saveFileList(files, FilePath.POST);

		return ResponseEntity.ok(savedFiles);
	}
}
