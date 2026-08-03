package com.common.backoffice.util.service;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;

import org.springframework.beans.factory.annotation.Qualifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class fileMultiService {

    /** 파일 업로드 결과: 저장된 파일명(생성ID+확장자) + 원본 파일명 */
    public record FileUploadResult(String savedFileName, String originalFileName) {}

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "gif", "jpg", "jpeg", "png", "xls", "xlsx", "mp4", "mp3", "avi", "mpeg"
    );


    @Resource(name = "egovFileIdGnrService")
    private EgovIdGnrService idgenService;

    /**
     * 단일 파일 업로드.
     * 저장 파일명 = egovFileIdGnrService 생성 ID + 확장자 (예: FILE_000000000000001.jpg)
     *
     * @return FileUploadResult(savedFileName, originalFileName), 허용되지 않는 확장자면 null
     */
    public FileUploadResult uploadFile(MultipartFile file, String filePath) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }

        int dot = originalFilename.lastIndexOf('.');
        String ext = (dot != -1) ? originalFilename.substring(dot + 1).toLowerCase() : "";

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            log.warn("허용되지 않는 확장자 업로드 시도: {}", ext);
            return null;
        }

        pathExist(filePath);

        String fileId = idgenService.getNextStringId();
        String savedName = ext.isBlank() ? fileId : fileId + "." + ext;

        Files.copy(file.getInputStream(), Paths.get(filePath, savedName));

        return new FileUploadResult(savedName, originalFilename);
    }

    /**
     * 다중 파일 업로드.
     * 개별 파일 실패 시 로그 남기고 계속 진행.
     *
     * @return 업로드 성공한 파일 결과 목록
     */
    public List<FileUploadResult> uploadFiles(List<MultipartFile> files, String filePath) {
        var results = new ArrayList<FileUploadResult>();
        for (var file : files) {
            try {
                var result = uploadFile(file, filePath);
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                log.error("파일 업로드 실패 - 파일명: {}", file.getOriginalFilename(), e);
            }
        }
        return results;
    }

    /**
     * 기존 코드와의 호환성 유지용 단일 반환 메서드.
     * 저장된 파일명(생성ID+확장자)만 반환.
     */
    public String uploadFileNm(List<MultipartFile> mpf, String filePath) {
        var results = uploadFiles(mpf, filePath);
        return results.isEmpty() ? "" : results.get(0).savedFileName();
    }



    public Map<String, String> getImageSize(File fileNm) {
        var map = new HashMap<String, String>();
        Metadata metadata = null;
        try {
            metadata = ImageMetadataReader.readMetadata(fileNm);
        } catch (ImageProcessingException | IOException e) {
            log.error("이미지 메타데이터 읽기 실패: {}", fileNm.getName(), e);
            return map;
        }

        for (var directory : metadata.getDirectories()) {
            for (var tag : directory.getTags()) {
                switch (tag.getTagName()) {
                    case "Image Width"  -> map.put("fileWidth",  tag.getDescription());
                    case "Image Height" -> map.put("fileHeight", tag.getDescription());
                    case "File Size"    -> map.put("fileSize",   tag.getDescription().replace(" bytes", ""));
                    default -> {}
                }
            }
        }
        return map;
    }

    public String fileSize(File f) {
        return f.exists() ? Long.toString(f.length()) : "0";
    }

    public boolean pathExist(String path) {
        try {
            Files.createDirectories(Paths.get(path));
            return true;
        } catch (IOException e) {
            log.error("디렉토리 생성 실패: {}", path, e);
            return false;
        }
    }

    public boolean deleteFile(String fileNm) {
        try {
            return Files.deleteIfExists(Paths.get(fileNm));
        } catch (IOException e) {
            log.debug("파일 삭제 실패: {}", fileNm, e);
            return false;
        }
    }

    public boolean deleteFile(String fileNm, String path) {
        try {
            return Files.deleteIfExists(Paths.get(path, fileNm));
        } catch (IOException e) {
            log.debug("파일 삭제 실패: {}/{}", path, fileNm, e);
            return false;
        }
    }
}

