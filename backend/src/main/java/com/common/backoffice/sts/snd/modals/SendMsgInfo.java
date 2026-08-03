package com.common.backoffice.sts.snd.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="SendMsgInfo : 컨텐츠 파일 상세 " )
public class SendMsgInfo {

	private String msgSeq;
	private String didId ;
	private String xmlProcessName;
	private String sendResult;
	private String didNm;
	private String processRemark;
	private String groupNm;
	private String centerId;
	private String schStartDay;
	private String schEndDay;
	private String sendRegDate;
	private String didMacAddress;
	private String didPlayTime;
	private String didIpAddr;
	private String errorMessage ;
	private String menuGubun;
	private String sendDidId;
	private String parentGroupId;
	private String groupId;
	private String groupCode;

	
	
}
