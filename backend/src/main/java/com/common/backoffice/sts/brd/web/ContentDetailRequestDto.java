package com.common.backoffice.sts.brd.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentDetailRequestDto {

    @NotBlank(message = "DB 처리 구분을 입력해 주세요.")
    @Schema(description = "DB 처리 구분", example = "Ins/Edt")
    private String mode;

    @Schema(description = "방송 상세 순번 (수정 시 필수)", example = "1")
    private String brodSeq;

    @NotBlank(message = "방송 코드를 입력해 주세요.")
    @Schema(description = "방송 코드", example = "BRD_000001")
    private String brodCode;

    @NotBlank(message = "첨부파일 ID를 입력해 주세요.")
    @Schema(description = "첨부파일 ID", example = "FILE_000000000000001")
    private String atchFileId;

    @Schema(description = "콘텐츠 시작일 (YYYYMMDD)", example = "20240101")
    private String contentStartDay;

    @Schema(description = "콘텐츠 종료일 (YYYYMMDD)", example = "20241231")
    private String contentEndDay;

    @Schema(description = "정렬 순서", example = "10")
    private String brodOrder;

    @Schema(description = "시간 간격 결과 (쉼표로 구분)", example = "000,010,020")
    private String timeIntervalResult;

    private String userId;
}