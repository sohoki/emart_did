package com.common.backoffice.sym.did.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="DidInfo : DID 정보 상세 " )
public class DidInfo {

    private String didId;
    private String didNm;
    private String didMac;
    private String didIpaddr;
    private String didSwver;
    private String centerId;
    private String didType;
    private String didResolution;
    private String didWidth;
    private String didHeight;
    private String didIptype;
    private String didModelType;
    private String didUseYn;
    private String didEndContime;
    private String didEndTime;
    private String frstRegistPnttm;
    private String frstRegisterId;
    private String lastRegistPnttm;
    private String lastUpdusrId;
    private String lastRegisterId;
    private String roleCode;
    private String didRemark;
    private String didSttus ;
    private String centerNm;
    private String roleNm;
    private String didSendInterval;
    private String mode;
	private String menuGubun;
	private String schCode;
	private String schName;
	private String groupCode;
	private String groupNm;
	private String codeNm;
	private String code;
	//신규 추가 
	private String didOs;
	private String didSerialtype;
	private String didSerialport;
	private String didMonitercnt;
	private String didStartTime;
	private String didTimeInterval;
	private String didSerialjavascript;
	private String parentGroupId;
	private String groupId;
	private String groupLevel;
	private String centerCnt;
	
	
	

	
}
