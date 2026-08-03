package com.common.backoffice.sts.xml.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="XmlInfo : 컨텐츠 파일 상세 " )
public class XmlInfo {

	private String xmlSeq;
	private String workGubun;
	private String xmlProcessName;
	private String processRemark;
	private String xmlInputParam;
	private String xmlOutputParam;
	private String resultCodeExample;
	private String etc1;
	private String etc2;
	private String etc3;
	private String testOk;
	private String xmlInputParamSample;
	private String xmlExplain;
	private String mode;
	
	private String frstRegistPnttm;
	private String lastRegistPnttm;
	private String frstRegisterId;
	private String lastRegisterId;
	private String codeNm;
	
	private String protType;
	
	private String menuGubun;	
	

}
