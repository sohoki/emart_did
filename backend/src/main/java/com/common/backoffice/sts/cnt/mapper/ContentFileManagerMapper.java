package com.common.backoffice.sts.cnt.mapper;


import com.common.backoffice.sts.cnt.modals.ContentFileInfo;
import com.common.backoffice.sts.cnt.modals.ContentFileInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface ContentFileManagerMapper {

	public List<ContentFileInfoVO> selectFilePageListByPagination (ContentFileInfoVO  searchVO);
	
	public List<ContentFileInfoVO> selectFileContentList (String conSeq);
	
	public List<ContentFileInfoVO> selectFileContentPageList (String detailSeq);
	
	public List<ContentFileInfoVO> selectFileContentLstDid (ContentFileInfoVO  searchVO);
	
	public List<ContentFileInfo> selectFileListCombo();
	
	public List<ContentFileInfoVO>  selectBasicFilePageListByPagination (ContentFileInfoVO  searchVO);

	public List<ContentFileInfoVO>  selectBasicFileDetailPageListByPagination (ContentFileInfoVO  searchVO);
	
	public int selectBasicFilePageListByPaginationTotCnt_S(ContentFileInfoVO  searchVO);
		
	public int selectFilePageListByPaginationTotCnt_S (ContentFileInfoVO  searchVO);
	
	public int selectFileListTotCnt_S  (String conSeq);
	
	public List<ContentFileInfoVO>  selectMediaConnList (String atchFileId);
	
	public ContentFileInfoVO selectFileDetail (String atchFileId);
	
	public int insertFileManage(ContentFileInfo vo);
	
	public int updateFileManage(ContentFileInfo vo);
	
	public int updateFileManageUseYn(ContentFileInfo vo);
	
	public int updateFileDetailInfo(ContentFileInfo vo);
	
	public int deleteFileManage(String atchFileId);
	
	public int selectFileContentTotCnt_S(String detailSeq);
	
	public int deleteFileConSeq(String conSeq);
}
