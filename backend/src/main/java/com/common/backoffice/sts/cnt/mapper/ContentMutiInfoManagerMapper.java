package com.common.backoffice.sts.cnt.mapper;

import com.common.backoffice.sts.cnt.modals.ContentMutiInfo;
import com.common.backoffice.sts.cnt.modals.ContentMutiInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;


@Mapper
public interface ContentMutiInfoManagerMapper {

	public List<ContentMutiInfoVO> selectContentMutiInfoManageListByPagination(ContentMutiInfoVO searchVO);
	
	//did 콘텐츠 리스트 현황 
	public List<ContentMutiInfoVO>   selectDIDContentLst(ContentMutiInfoVO searchVO);
	
	public  List<ContentMutiInfoVO>  selectNextContentMutiInfo (String conSeq);
	
	public int selectContentMutiInfoManageListTotCnt_S(ContentMutiInfoVO searchVO);
	
	public ContentMutiInfoVO selectContentMutiInfoManageDetail(String conSeq);
	
	public ContentMutiInfoVO selectContentMutiInfoManageView(String conSeq);
	
	//신규 
	public List<ContentMutiInfo> selectNextSeqList (String conSeq);
	
	
	public String selectContentFileInfo (String conSeq);
	
	public String selectContentFileInfoLocal(String conSeq);
	
	public String selectMaxTimeInterval(String conSeq);
	
	public String selectMaxSeqInfo();
	
	public int insertContentMutiInfoManage(ContentMutiInfo vo);
	
	public int updateContentMutiInfoManage(ContentMutiInfo vo);
	
	public int updateContentMutiFile (ContentMutiInfo vo);
	
	public int updateContentMutiFileLocal (ContentMutiInfo vo);
	
	public int deleteContentMutiInfoManage(String conSeq);
	
	
}

