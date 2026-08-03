package com.common.backoffice.sym.rnt.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="AuthorInfoVO : DID 정보 상세 " )
public class AuthorInfo {

	private String authorCode;
	private String authorNm;
	private String authorDc;
	

}
