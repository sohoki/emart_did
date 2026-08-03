package com.common.backoffice.sts.cnt.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="ContentMessageInfo : 컨텐츠 파일 상세 " )
public class ContentMessageInfo {

    private String mode;
	private String sendDidId;
	private String didId;
	private String groupCode;
	private String sendMessage;
	private String sendMessageStartDay;
	private String sendMessageEndDay;
	private String sendMessageStartTime;
	private String sendMessageEndTime;
	private String sendFontType;
	private String sendUseYn;
	private String sendRegDate;
	private String sendDidCheckDate;
	private String didNm;
	private String groupNm;
	private String message_atch;
	private String sendMessageStartHour;
	private String sendMessageStartMin;
	private String sendMessageEndHour;
	private String sendMessageEndMin;
	private String sendMsgId;
	
	
	

	
	
}
