package net.causw.adapter.web;

import lombok.RequiredArgsConstructor;
import net.causw.application.dto.file.FileResponseDto;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.model.enums.uuidFile.FilePath;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/storage", produces = APPLICATION_JSON_VALUE)
public class StorageController {

    private final UuidFileService uuidFileService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public FileResponseDto post(
            @RequestPart("file") MultipartFile multipartFile,
            @RequestParam("type") FilePath filePath
    ) {
        return FileResponseDto.from(uuidFileService.saveFile(multipartFile, filePath).getFileUrl());
    }

}
