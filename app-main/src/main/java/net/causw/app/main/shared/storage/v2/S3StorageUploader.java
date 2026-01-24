package net.causw.app.main.shared.storage.v2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.shared.storage.v2.dto.FileMetadata;
import net.causw.app.main.shared.storage.v2.dto.StorageResult;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AWS S3 스토리지 업로더 구현체
 */
@Slf4j
@MeasureTime
@Component("s3Uploader")
@Profile("!local")
@RequiredArgsConstructor
public class S3StorageUploader implements StorageUploader {

	private final AmazonS3Client amazonS3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@Override
	public StorageResult upload(MultipartFile file, FileMetadata metadata) {
		String fileKey = metadata.fileKey();

		ObjectMetadata objectMetadata = createObjectMetadata(file, metadata);

		try (InputStream inputStream = file.getInputStream()) {
			amazonS3Client.putObject(
				new PutObjectRequest(bucketName, fileKey, inputStream, objectMetadata)
					.withCannedAcl(CannedAccessControlList.PublicRead));

			String fileUrl = amazonS3Client.getUrl(bucketName, fileKey).toString().trim();

			log.debug("File uploaded successfully to S3. FileKey: {}, FileUrl: {}", fileKey, fileUrl);

			return StorageResult.of(
				fileKey,
				fileUrl,
				file.getSize(),
				Instant.now());

		} catch (IOException e) {
			log.error("Failed to upload file to S3. FileKey: {}, Error: {}", fileKey, e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.FILE_UPLOAD_FAIL,
				MessageUtil.FILE_UPLOAD_FAIL + e.getMessage());
		}
	}

	@Override
	public void delete(String fileKey) {
		try {
			amazonS3Client.deleteObject(bucketName, fileKey);
			log.debug("File deleted successfully from S3. FileKey: {}", fileKey);

		} catch (Exception e) {
			log.error("Failed to delete file from S3. FileKey: {}, Error: {}", fileKey, e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.FILE_DELETE_FAIL,
				MessageUtil.FILE_DELETE_FAIL + e.getMessage());
		}
	}

	private ObjectMetadata createObjectMetadata(MultipartFile file, FileMetadata metadata) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(file.getContentType());

		// Content-Disposition : attachment 헤더 추가
		// 브라우저가 파일을 열지 않고 다운로드하도록 설정
		String contentDisposition = ContentDisposition.builder("attachment")
			.filename(metadata.originalFileName(), StandardCharsets.UTF_8)
			.build()
			.toString();
		objectMetadata.setContentDisposition(contentDisposition);

		return objectMetadata;
	}
}
