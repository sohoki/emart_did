package com.common.backoffice.sts.brd.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentDetailCopyRequestDto {

    @NotBlank(message = "복사할 방송 코드를 입력해 주세요.")
    @Schema(description = "복사할 방송 코드(원본)", example = "BRD_000001")
    private String preBrodCode;

    @NotBlank(message = "대상 방송 코드를 입력해 주세요.")
    @Schema(description = "대상 방송 코드(사본)", example = "BRD_000002")
    private String brodCode;
}