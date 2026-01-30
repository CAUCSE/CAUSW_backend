package net.causw.app.main.domain.asset.file.service.v2.implementation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.repository.UuidFileRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 조회 전담 클래스
 */
@Slf4j
@MeasureTime
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileReader {

	private final UuidFileRepository uuidFileRepository;

	/**
	 * ID로 파일 조회
	 *
	 * @param id 파일 ID
	 * @return 파일 엔티티
	 * @throws BadRequestException 파일이 존재하지 않을 경우
	 */
	public UuidFile findById(@NotBlank String id) {
		return uuidFileRepository.findById(id)
			.orElseThrow(() -> {
				log.warn("File not found by ID: {}", id);
				return new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.FILE_NOT_FOUND);
			});
	}

	/**
	 * 파일 URL로 파일 조회
	 *
	 * @param fileUrl 파일 URL
	 * @return 파일 엔티티
	 * @throws BadRequestException 파일이 존재하지 않을 경우
	 */
	public UuidFile findByFileUrl(@NotBlank String fileUrl) {
		return uuidFileRepository.findByFileUrl(fileUrl)
			.orElseThrow(() -> {
				log.warn("File not found by URL: {}", fileUrl);
				return new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.FILE_NOT_FOUND);
			});
	}

	/**
	 * ID 목록으로 파일들 조회
	 *
	 * @param ids 파일 ID 목록
	 * @return 파일 엔티티 목록
	 */
	public List<UuidFile> findByIds(List<String> ids) {
		return uuidFileRepository.findAllById(ids);
	}

	/**
	 * ID로 파일 조회 (Optional 반환)
	 *
	 * @param id 파일 ID
	 * @return 파일 엔티티 Optional
	 */
	public Optional<UuidFile> findByIdOptional(@NotBlank String id) {
		return uuidFileRepository.findById(id);
	}
}
