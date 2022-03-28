package net.causw.application;

import net.causw.application.dto.UploadFileResponseDto;
import net.causw.domain.model.ImageLocation;
import net.causw.domain.validation.AttachmentSizeValidator;
import net.causw.domain.validation.ImageExtensionValidator;
import net.causw.domain.validation.ImageSizeValidator;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.infrastructure.GcpFileUploader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class StorageService {
    private final GcpFileUploader gcpFileUploader;

    public StorageService(GcpFileUploader gcpFileUploader) {
        this.gcpFileUploader = gcpFileUploader;
    }

    public UploadFileResponseDto uploadAttachmentToGcs(MultipartFile attachment) {
        ValidatorBucket.of()
                .consistOf(AttachmentSizeValidator.of(attachment))
                .validate();

        return UploadFileResponseDto.of(
                gcpFileUploader.uploadFileToGcp(attachment)
        );
    }

    public UploadFileResponseDto uploadImageToGcs(MultipartFile image, Optional<String> imageLocation) {
        ValidatorBucket.of()
                .consistOf(ImageSizeValidator.of(image))
                .consistOf(ImageExtensionValidator.of(image))
                .validate();

        return UploadFileResponseDto.of(
                gcpFileUploader.uploadImageToGcp(image, ImageLocation.of(imageLocation.orElse("ETC")))
        );
    }
}
