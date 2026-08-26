package net.causw.app.main.shared.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.shared.storage.dto.FileMetadata;
import net.causw.app.main.shared.storage.dto.PresignedUploadResult;
import net.causw.app.main.shared.storage.dto.StorageResult;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.InternalServerException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * AWS S3 스토리지 업로더 구현체
 */
@Slf4j
@MeasureTime
@Component("s3StorageClient")
@Profile("!local")
@RequiredArgsConstructor
public class S3StorageClient implements StorageClient {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;

	@Override
	public StorageResult upload(MultipartFile file, FileMetadata metadata) {
		String fileKey = metadata.fileKey();

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(fileKey)
			.contentType(metadata.contentType())
			.contentLength(metadata.fileSize())
			.contentDisposition(createContentDisposition(metadata))
			.acl(ObjectCannedACL.PUBLIC_READ)
			.build();

		try (InputStream inputStream = file.getInputStream()) {
			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, metadata.fileSize()));

			String fileUrl = s3Client.utilities()
				.getUrl(GetUrlRequest.builder().bucket(bucketName).key(fileKey).build())
				.toString()
				.trim();

			log.debug("File uploaded successfully to S3. FileKey: {}, FileUrl: {}", fileKey, fileUrl);

			return StorageResult.of(
				fileKey,
				fileUrl,
				metadata.fileSize(),
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
			s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileKey).build());
			log.debug("File deleted successfully from S3. FileKey: {}", fileKey);

		} catch (Exception e) {
			log.error("Failed to delete file from S3. FileKey: {}, Error: {}", fileKey, e.getMessage(), e);
			throw new InternalServerException(
				ErrorCode.FILE_DELETE_FAIL,
				MessageUtil.FILE_DELETE_FAIL + e.getMessage());
		}
	}

	@Override
	public PresignedUploadResult generatePresignedUploadUrl(FileMetadata metadata, Duration expiry) {
		String fileKey = metadata.fileKey();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(expiry)
			.putObjectRequest(PutObjectRequest.builder()
				.bucket(bucketName)
				.key(fileKey)
				.contentType(metadata.contentType())
				.contentLength(metadata.fileSize())
				.build())
			.build();

		PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

		String fileUrl = s3Client.utilities()
			.getUrl(GetUrlRequest.builder().bucket(bucketName).key(fileKey).build())
			.toString()
			.trim();

		log.debug("Generated presigned upload URL. FileKey: {}, ExpiresAt: {}", fileKey, presigned.expiration());

		return PresignedUploadResult.of(presigned.url().toString(), fileUrl, presigned.expiration());
	}

	@Override
	public List<String> deleteAll(List<String> fileKeys) {
		if (fileKeys.isEmpty()) {
			return List.of();
		}

		List<ObjectIdentifier> objects = fileKeys.stream()
			.map(key -> ObjectIdentifier.builder().key(key).build())
			.toList();

		DeleteObjectsRequest request = DeleteObjectsRequest.builder()
			.bucket(bucketName)
			.delete(Delete.builder().objects(objects).quiet(false).build())
			.build();

		DeleteObjectsResponse response = s3Client.deleteObjects(request);

		List<S3Error> errors = response.errors();
		if (!errors.isEmpty()) {
			errors.forEach(
				e -> log.warn("[S3 Bulk Delete] 삭제 실패. key={}, code={}, message={}", e.key(), e.code(), e.message()));
		}

		return response.deleted().stream()
			.map(deleted -> deleted.key())
			.toList();
	}

	@Override
	public boolean exists(String fileKey) {
		try {
			s3Client.headObject(HeadObjectRequest.builder()
				.bucket(bucketName)
				.key(fileKey)
				.build());
			return true;
		} catch (NoSuchKeyException e) {
			log.debug("File does not exist in S3. FileKey: {}", fileKey);
			return false;
		}
	}

	private String createContentDisposition(FileMetadata metadata) {
		// Content-Disposition : attachment 헤더 추가
		// 브라우저가 파일을 열지 않고 다운로드하도록 설정
		return ContentDisposition.builder("attachment")
			.filename(metadata.originalFileName(), StandardCharsets.UTF_8)
			.build()
			.toString();
	}
}
