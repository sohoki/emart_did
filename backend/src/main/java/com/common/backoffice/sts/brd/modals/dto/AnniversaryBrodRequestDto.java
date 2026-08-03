package com.common.backoffice.sts.brd.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnniversaryBrodRequestDto {

    @NotBlank(message = "방송 코드를 입력해 주세요.")
    @Schema(description = "방송 코드", example = "BRD_000001")
    private String brodCode;

    @NotBlank(message = "기념일 순번을 입력해 주세요.")
    @Schema(description = "기념일 순번", example = "1")
    private String anniverSeq;

    @NotBlank(message = "체크 여부를 입력해 주세요.")
    @Schema(description = "체크 여부 (Y/N)", example = "Y")
    private String checkVal;
}
