package com.common.backoffice.sts.cnt.modals;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title="ContentDetailFileInfo : 컨텐츠 파일 상세 " )
public class ContentDetailFileInfo {

	private String fileSeq;
	private String detailSeq;
	private String conSeq;
	private String atchFileId;
	private String timeInterval ="";
	private String fileOrder;
	private String regDate;
	private String fileStreCours;
	
	

}