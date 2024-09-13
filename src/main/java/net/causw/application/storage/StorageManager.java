package net.causw.application.storage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.FilePath;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.S3Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StorageManager {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    protected Map<FileInfo, String> uploadFile(MultipartFile multipartFile, String rawFileName, String extension, FilePath filePath, String uuid) {

        String fileKey = this.buildFileKey(uuid, rawFileName, extension, filePath);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileKey, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new InternalServerException(ErrorCode.FILE_UPLOAD_FAIL, MessageUtil.FILE_UPLOAD_FAIL + e.getMessage());
        }

        Map<FileInfo, String> fileInfoStringMap = new HashMap<>();
        fileInfoStringMap.put(FileInfo.FILE_URL, amazonS3Client.getUrl(bucketName, fileKey).toString().trim());
        fileInfoStringMap.put(FileInfo.FILE_KEY, fileKey);
        return fileInfoStringMap;
    }

    protected void deleteFile(String fileKey) {
        try {
            amazonS3Client.deleteObject(bucketName, fileKey);
        } catch (Exception e) {
            throw new InternalServerException(ErrorCode.FILE_DELETE_FAIL, MessageUtil.FILE_DELETE_FAIL + e.getMessage());
        }
    }

    private String buildFileKey(String uuid, String rawFileName, String extension, FilePath filePath) {
        return filePath.getDirectory() + "/" + rawFileName + "_" + uuid + "." + extension;
    }

    protected enum FileInfo {
        FILE_URL,
        FILE_KEY
    }

}
