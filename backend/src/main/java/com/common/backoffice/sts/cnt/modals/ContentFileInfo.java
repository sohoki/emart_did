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
@Schema(title="ContentFileInfo : 컨텐츠 파일 상세 " )
public class ContentFileInfo {

	private String atchFileId;
	private String fileStreCours;
	private String streFileNm;
	private String orignlFileNm;
	private String fileExtsn;
	private String fileSize;
	private String conSeq;
	private String fileOrder;
	private String detailSeq;
	
	private String hisSeq;
	private String schCode;
	private String didId;
	private String didMac;
	private String mode;
	private String fileThumnail;
	private String fileGubun;

	//신규
	private String timeInterval;
	private String mediaType;
	private String conType;
	private String playTime;
	private String fileWidth;
	private String fileHeight;
	
	private String useYn;
	private String notConType;
	
	private String fileRegdate;
	
	private String fileAlbum = "";
	private String singerNm = "";
	
	private String frstRegistPnttm;
	private String lastRegistPnttm;
	private String frstRegisterId;
	private String lastRegisterId;

}
