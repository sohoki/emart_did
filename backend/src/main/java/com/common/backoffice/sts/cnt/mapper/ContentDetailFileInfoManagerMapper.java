package com.common.backoffice.sts.cnt.mapper;



import com.common.backoffice.sts.cnt.modals.ContentDetailFileInfo;
import com.common.backoffice.sts.cnt.modals.ContentDetailFileInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface ContentDetailFileInfoManagerMapper {

	public List<ContentDetailFileInfoVO> selectContentDetailFileLst(ContentDetailFileInfoVO searchVO);
	
	public ContentDetailFileInfoVO selectContentDetailFileInfo(String atchFileId);
	
	public ContentDetailFileInfoVO selectContentDetailFileInfoFileSeq (String fileSeq);
	
	
	public String selectDetailContentSumTime (String detailSeq);
	
	public int selectPageSeqCheckFilePageCnt(ContentDetailFileInfoVO searchVO);
	
	public int selectTimeIntevalNullCheck(String conSeq);
	
	public int selectMaxfileSeq(ContentDetailFileInfo vo);

	public int insertContentDetailFileManage(ContentDetailFileInfo vo);
	
	public int updateContentDetailFileManage(ContentDetailFileInfo vo);
	
	public int updateContentOrderDetailFileManage(ContentDetailFileInfo vo);
	
	public int updateContentDetailFileTimeIntervalManage(ContentDetailFileInfo vo);
	
	public int deleteContentDetailFileManage(String fileSeq);
	
	
}
