package net.causw.app.main.controller;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.dto.file.FileResponseDto;
import net.causw.app.main.service.uuidFile.UuidFileService;
import net.causw.app.main.domain.model.enums.uuidFile.FilePath;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/storage", produces = APPLICATION_JSON_VALUE)
public class StorageController {

    private final UuidFileService uuidFileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public FileResponseDto post(
            @RequestPart("file") MultipartFile multipartFile,
            @RequestParam("type") FilePath filePath
    ) {
        return FileResponseDto.from(uuidFileService.saveFile(multipartFile, filePath).getFileUrl());
    }

}
