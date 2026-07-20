package net.causw.app.main.domain.community.post.service.implementation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.implementation.FileReader;
import net.causw.app.main.domain.asset.file.service.implementation.FileWriter;
import net.causw.app.main.domain.asset.file.service.implementation.PostAttachImageWriter;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.dto.ImageCreateMeta;
import net.causw.app.main.domain.community.post.service.dto.ImageUpdateMeta;
import net.causw.app.main.domain.community.post.service.util.PostValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 게시글 첨부 이미지의 업로드·삭제·재정렬 등 이미지 처리 책임을 전담하는 컴포넌트.
 * <p>
 * PostService가 비즈니스 흐름(검증, 트랜잭션 등)에 집중할 수 있도록
 * 이미지 관련 세부 로직을 이 클래스에서 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostImageManager {

	private final FileWriter fileWriter;
	private final FileReader fileReader;
	private final PostAttachImageWriter postAttachImageWriter;

	/**
	 * 게시글 생성 시 이미지를 업로드하고 PostAttachImage 리스트를 구성합니다.
	 *
	 * @param post        저장된 게시글 엔티티
	 * @param imageFiles  업로드할 파일 목록 (nullable)
	 * @param imageMetas  이미지 메타데이터 목록 (nullable)
	 * @return order 기준 정렬된 PostAttachImage 리스트
	 */
	public List<PostAttachImage> uploadAndBuildForCreate(
		Post post,
		List<MultipartFile> imageFiles,
		List<ImageCreateMeta> imageMetas) {

		int fileCount = (imageFiles != null) ? imageFiles.size() : 0;
		if (fileCount == 0) {
			return List.of();
		}
		PostValidator.validateCreateImageMetas(imageMetas, fileCount);

		List<UuidFile> uploadedFiles = uploadFiles(imageFiles);

		List<PostAttachImage> result = new ArrayList<>();

		if (imageMetas != null && !imageMetas.isEmpty()) {
			for (ImageCreateMeta meta : imageMetas) {
				UuidFile file = uploadedFiles.get(meta.fileIndex());
				result.add(PostAttachImage.of(post, file, meta.order(), meta.isRepresentative()));
			}
		} else {
			// imageMetas가 없으면 기존 방식으로 순서대로 생성 (하위 호환)
			for (int i = 0; i < uploadedFiles.size(); i++) {
				result.add(PostAttachImage.of(post, uploadedFiles.get(i), i, i == 0));
			}
		}

		result.sort(Comparator.comparingInt(PostAttachImage::getImageOrder));
		return result;
	}

	/**
	 * 게시글 수정 시 기존 이미지와 새 이미지를 병합하여 최종 PostAttachImage 리스트를 구성합니다.
	 * <ul>
	 *   <li>type=EXISTING → 기존 이미지를 유지하고 order/isRepresentative만 업데이트</li>
	 *   <li>type=NEW → 새 파일을 업로드하여 PostAttachImage 생성</li>
	 *   <li>imageMetas에 포함되지 않은 기존 이미지 → DB에서 삭제 (파일 ID 반환)</li>
	 * </ul>
	 *
	 * @param post           게시글 엔티티
	 * @param newImageFiles  새로 업로드할 파일 목록 (nullable)
	 * @param imageMetas     이미지 메타데이터 목록 (nullable)
	 * @return 처리 결과 (최종 이미지 리스트 + 삭제된 파일 ID 목록)
	 */
	public ImageUpdateResult mergeAndBuildForUpdate(
		Post post,
		List<MultipartFile> newImageFiles,
		List<ImageUpdateMeta> imageMetas) {

		List<PostAttachImage> currentImages = post.getPostAttachImageList();

		// 기존 PostAttachImage를 URL → 엔티티 맵으로 구성
		Map<String, PostAttachImage> existingByUrl = currentImages.stream()
			.collect(Collectors.toMap(
				img -> img.getUuidFile().getFileUrl(),
				img -> img));

		// 이미지 메타데이터 검증
		int newFileCount = (newImageFiles != null) ? newImageFiles.size() : 0;
		PostValidator.validateUpdateImageMetas(imageMetas, newFileCount, existingByUrl.keySet());

		// 유지할 기존 이미지 URL 목록 추출
		Set<String> existingUrlsToKeep = (imageMetas != null)
			? imageMetas.stream()
				.filter(meta -> meta.type() == ImageUpdateMeta.Type.EXISTING)
				.map(ImageUpdateMeta::url)
				.collect(Collectors.toSet())
			: Set.of();

		// 삭제 대상 분리 및 DB 삭제
		List<PostAttachImage> imagesToDelete = currentImages.stream()
			.filter(img -> !existingUrlsToKeep.contains(img.getUuidFile().getFileUrl()))
			.toList();

		List<String> deletedFileIds = imagesToDelete.stream()
			.map(it -> it.getUuidFile().getId())
			.toList();

		if (!imagesToDelete.isEmpty()) {
			postAttachImageWriter.deleteAllInBatch(imagesToDelete);
		}

		// 새 이미지 파일 업로드
		List<UuidFile> newUploadedFiles = uploadFiles(newImageFiles);

		// 최종 PostAttachImage 리스트 구성
		List<PostAttachImage> finalImages = new ArrayList<>();
		List<PostAttachImage> newImages = new ArrayList<>();
		if (imageMetas != null && !imageMetas.isEmpty()) {
			for (ImageUpdateMeta meta : imageMetas) {
				if (meta.type() == ImageUpdateMeta.Type.EXISTING) {
					PostAttachImage existing = existingByUrl.get(meta.url());
					existing.setImageOrder(meta.order());
					existing.setIsRepresentative(meta.isRepresentative());
					finalImages.add(existing);
				} else {
					UuidFile newFile = newUploadedFiles.get(meta.fileIndex());
					newImages.add(PostAttachImage.of(post, newFile, meta.order(), meta.isRepresentative()));
				}
			}
		}

		// 신규 이미지를 명시적으로 저장하여 post_id가 올바르게 설정되도록 합니다.
		if (!newImages.isEmpty()) {
			List<PostAttachImage> savedNewImages = postAttachImageWriter.saveAll(newImages);
			finalImages.addAll(savedNewImages);
		}

		finalImages.sort(Comparator.comparingInt(PostAttachImage::getImageOrder));
		return new ImageUpdateResult(finalImages, deletedFileIds);
	}

	/**
	 * 삭제 대상 파일을 실제 스토리지에서 삭제합니다.
	 * 트랜잭션 커밋 이후 호출되어야 합니다.
	 *
	 * @param deletedFileIds 삭제할 UuidFile ID 목록
	 */
	public void deleteOrphanedFiles(List<String> deletedFileIds) {
		if (deletedFileIds == null || deletedFileIds.isEmpty()) {
			return;
		}
		List<UuidFile> files = fileReader.findByIds(deletedFileIds);
		fileWriter.deleteList(files);
	}

	// ── 헬퍼 ──────────────────────────────────────────────────────────

	private List<UuidFile> uploadFiles(List<MultipartFile> files) {
		if (files == null || files.isEmpty()) {
			return List.of();
		}
		return fileWriter.uploadAndSaveList(files, FilePath.POST);
	}

	/**
	 * 게시글 수정 시 이미지 처리 결과를 담는 DTO.
	 *
	 * @param finalImages    order 기준 정렬된 최종 PostAttachImage 리스트
	 * @param deletedFileIds 스토리지에서 삭제해야 할 UuidFile ID 목록
	 */
	public record ImageUpdateResult(
		List<PostAttachImage> finalImages,
		List<String> deletedFileIds) {
	}
}
