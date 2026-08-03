package com.common.backoffice.sts.cnt.service;



import java.util.List;

import com.common.backoffice.sts.cnt.mapper.ContentDetailFileInfoManagerMapper;
import com.common.backoffice.sts.cnt.modals.ContentDetailFileInfo;
import com.common.backoffice.sts.cnt.modals.ContentDetailFileInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ContentDetailFileInfoManageService  {

	
	private final ContentDetailFileInfoManagerMapper contentFileDetail;
	
	
	
	public List<ContentDetailFileInfoVO> selectContentDetailFileLst(
			ContentDetailFileInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return contentFileDetail.selectContentDetailFileLst(searchVO);
	}

	
	public int selectPageSeqCheckFilePageCnt(ContentDetailFileInfoVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return contentFileDetail.selectPageSeqCheckFilePageCnt(searchVO);
	}
    public int selectMaxfileSeq(ContentDetailFileInfo vo)
            throws Exception {
        // TODO Auto-generated method stub
        return contentFileDetail.selectMaxfileSeq(vo);
    }
    public ContentDetailFileInfoVO selectContentDetailFileInfoFileSeq(String fileSeq) throws Exception {
        // TODO Auto-generated method stub
        return contentFileDetail.selectContentDetailFileInfoFileSeq(fileSeq);
    }
    public String selectDetailContentSumTime(String detailSeq) throws Exception {
        // TODO Auto-generated method stub
        return contentFileDetail.selectDetailContentSumTime(detailSeq);
    }
    public int selectTimeIntevalNullCheck(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return contentFileDetail.selectTimeIntevalNullCheck(conSeq);
    }

    public ContentDetailFileInfoVO selectContentDetailFileInfo(String atchFileId)
            throws Exception {
        // TODO Auto-generated method stub
        return contentFileDetail.selectContentDetailFileInfo(atchFileId);
    }

    @Transactional(readOnly = false)
	public int insertContentDetailFileManage(ContentDetailFileInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return contentFileDetail.insertContentDetailFileManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateContentDetailFileManage(ContentDetailFileInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return contentFileDetail.updateContentDetailFileManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteContentDetailFileManage(String  fileSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return contentFileDetail.deleteContentDetailFileManage(fileSeq);
	}

    @Transactional(readOnly = false)
	public int updateContentOrderDetailFileManage(ContentDetailFileInfo vo)
			throws Exception {
		// TODO Auto-generated method stub
		return contentFileDetail.updateContentOrderDetailFileManage(vo);
	}

	


    @Transactional(readOnly = false)
	public int updateContentDetailFileTimeIntervalManage(
			ContentDetailFileInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return contentFileDetail.updateContentDetailFileTimeIntervalManage(vo);
	}

	


	

	

}
