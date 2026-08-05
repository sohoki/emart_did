package com.common.backoffice.sym.grp.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="GroupDidInfo : DID 정보 상세 " )
public class GroupInfo {

    private String mode;
	private String groupCode;
	private String groupNm;
	private String groupUseYn;
	private String didCnt;
	private String menuGubun;

}
