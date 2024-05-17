package net.causw.application.storage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import net.causw.domain.model.enums.ImageLocation;
import net.causw.domain.model.util.S3Util;
import net.causw.domain.validation.ImageLocationTypeValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile multipartFile, String type) {

        ValidatorBucket.of()
                .consistOf(ImageLocationTypeValidator.of(type)).validate();

        ImageLocation imageLocation = ImageLocation.of(type);

        String fileName = S3Util.buildFileName(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        String filePath = imageLocation.getValue() + "/" + fileName;

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucketName, filePath, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return amazonS3Client.getUrl(bucketName, filePath).toString().trim();
    }

}
