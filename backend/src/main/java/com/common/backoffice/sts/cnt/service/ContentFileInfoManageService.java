package com.common.backoffice.sts.cnt.service;

import java.util.List;

import com.common.backoffice.sts.cnt.mapper.ContentFileManagerMapper;
import com.common.backoffice.sts.cnt.modals.ContentFileInfo;
import com.common.backoffice.sts.cnt.modals.ContentFileInfoVO;
import egovframework.com.cmm.service.Globals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ContentFileInfoManageService  {

	private final ContentFileManagerMapper conFileManager;

	
	public List<ContentFileInfoVO> selectFilePageListByPagination(
			ContentFileInfoVO searchVO) {
		// TODO Auto-generated method stub
		
		return conFileManager.selectFilePageListByPagination(searchVO);
	}

	
	public List<ContentFileInfoVO> selectFileContentList(String conSeq) {
		// TODO Auto-generated method stub
		return conFileManager.selectFileContentList(conSeq);
	}

	
	public int selectFilePageListByPaginationTotCnt_S(ContentFileInfoVO searchVO) {
		// TODO Auto-generated method stub
		return conFileManager.selectFilePageListByPaginationTotCnt_S(searchVO);
	}

	
	public int selectFileListTotCnt_S(String conSeq) {
		// TODO Auto-generated method stub
		return conFileManager.selectFileListTotCnt_S(conSeq);
	}

	
	public ContentFileInfoVO selectFileDetail(String atchFileId) {
		// TODO Auto-generated method stub
		return conFileManager.selectFileDetail(atchFileId);
	}

    public List<ContentFileInfo> selectFileListCombo() throws Exception {
        // TODO Auto-generated method stub
        return conFileManager.selectFileListCombo();
    }


    public List<ContentFileInfoVO> selectBasicFilePageListByPagination(
            ContentFileInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return conFileManager.selectBasicFilePageListByPagination(searchVO);
    }


    public int selectBasicFilePageListByPaginationTotCnt_S(
            ContentFileInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return conFileManager.selectBasicFilePageListByPaginationTotCnt_S(searchVO);
    }


    public List<ContentFileInfoVO> selectMediaConnList(
            String atchFileId) throws Exception {
        // TODO Auto-generated method stub
        return conFileManager.selectMediaConnList(atchFileId);
    }


    public List<ContentFileInfoVO> selectBasicFileDetailPageListByPagination(
            ContentFileInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return conFileManager.selectBasicFileDetailPageListByPagination(searchVO);
    }

    public List<ContentFileInfoVO> selectFileContentPageList(	String detailSeq) throws Exception {
        // TODO Auto-generated method stub
        return conFileManager.selectFileContentPageList(detailSeq);
    }


    public int selectFileContentTotCnt_S(String detailSeq) throws Exception {
        // TODO Auto-generated method stub
        return conFileManager.selectFileContentTotCnt_S(detailSeq);
    }

    public List<ContentFileInfoVO> selectFileContentLstDid(
            ContentFileInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return conFileManager.selectFileContentLstDid(searchVO);
    }

    @Transactional(readOnly = false)
	public int insertFileManage(ContentFileInfo vo) {
		// TODO Auto-generated method stub
		return vo.getMode().equals(Globals.SAVE_MODE_INSERT) ?
                conFileManager.insertFileManage(vo):
                conFileManager.updateFileManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateFileManage(ContentFileInfo vo) {
		// TODO Auto-generated method stub
		return conFileManager.updateFileManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateFileDetailInfo(ContentFileInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return conFileManager.updateFileDetailInfo(vo);
	}

    @Transactional(readOnly = false)
	public int deleteFileManage(String atchFileId) {
		// TODO Auto-generated method stub
		return conFileManager.deleteFileManage(atchFileId);
	}



    @Transactional(readOnly = false)
	public int deleteFileConSeq(String conSeq) throws Exception {
		// TODO Auto-generated method stub
		return conFileManager.deleteFileConSeq(conSeq);
	}

	


    @Transactional(readOnly = false)
	public int updateFileManageUseYn(ContentFileInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return conFileManager.updateFileManageUseYn(vo);
	}

	


	
	
	
}
