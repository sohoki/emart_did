package com.common.backoffice.sts.cnt.mapper;

import com.common.backoffice.sts.cnt.modals.ContentDetailInfo;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;




@Mapper
public interface ContentDetailManagerMapper {

	public List<ContentDetailInfo> selectContentDetailLst(String conSeq );
	
	public ContentDetailInfo selectContentDetail(String detailSeq );
	
	public List<ContentDetailInfo> selectConDetailCombo(String conSeq); 
	
	public List<ContentDetailInfo>  selectContentDetailDidPage(ContentDetailInfo searchVO);
	
	
	public int insertContentDetailManage (ContentDetailInfo vo);
	
	public int updateContentDetailManage(ContentDetailInfo vo);
	
	
	public int updateContentDetailTimeManage(String detailSeq);
	
	public  int deleteContentDetailManage(String detailSeq );
	
	public int selectMaxDetail();
	
	public int selectPageSeqCheck(String conSeq, String detailOrder);
	
	public int selectPageSeqCheckPage01(String conSeq);
	
	public int selectPageSeqCheckPage02(String conSeq);
	
	public int selectPageSeqCheckPage01Cnt(String conSeq);
	
	public int selectPageSeqCheckPage02Cnt(String conSeq);
	
	public int selectConDetailCnt(String conSeq);
	
	
	public int deleteContentDetailConSeq(String conSeq);
	
	
}
