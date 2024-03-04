package net.causw.adapter.web;

import lombok.RequiredArgsConstructor;
import net.causw.application.dto.file.FileResponseDto;
import net.causw.application.storage.StorageService;
import net.causw.domain.model.enums.ImageLocation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/storage", produces = APPLICATION_JSON_VALUE)
public class StorageController {

    private final StorageService storageService;

    @PostMapping("")
    public FileResponseDto post(
            @RequestPart("file") MultipartFile multipartFile, @RequestParam String type) {
        return FileResponseDto.from(storageService.uploadFile(multipartFile,type));
    }
}
