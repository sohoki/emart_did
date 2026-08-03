package com.common.backoffice.sts.cnt.service;

import java.util.List;

import com.common.backoffice.sts.cnt.mapper.ContentMutiInfoManagerMapper;
import com.common.backoffice.sts.cnt.modals.ContentMutiInfo;
import com.common.backoffice.sts.cnt.modals.ContentMutiInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ContentMutimanageService {


	private final ContentMutiInfoManagerMapper conMulti;

	
	public List<ContentMutiInfoVO> selectContentMutiInfoManageListByPagination(
			ContentMutiInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return conMulti.selectContentMutiInfoManageListByPagination(searchVO);
	}

	
	public int selectContentMutiInfoManageListTotCnt_S(
			ContentMutiInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return conMulti.selectContentMutiInfoManageListTotCnt_S(searchVO);
	}

	
	public ContentMutiInfoVO selectContentMutiInfoManageDetail(String conSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return conMulti.selectContentMutiInfoManageDetail(conSeq);
	}

    public ContentMutiInfoVO selectContentMutiInfoManageView(String conSeq)
            throws Exception {
        // TODO Auto-generated method stub
        return conMulti.selectContentMutiInfoManageView(conSeq);
    }


    public List<ContentMutiInfoVO> selectDIDContentLst(
            ContentMutiInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return conMulti.selectDIDContentLst(searchVO);
    }

    public String selectContentFileInfo(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return conMulti.selectContentFileInfo(conSeq);
    }


    public List<ContentMutiInfoVO> selectNextContentMutiInfo(String conSeq)
            throws Exception {
        // TODO Auto-generated method stub
        return conMulti.selectNextContentMutiInfo(conSeq);
    }


    public List<ContentMutiInfo> selectNextSeqList(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return conMulti.selectNextSeqList(conSeq) ;
    }


    public String selectMaxTimeInterval(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return conMulti.selectMaxTimeInterval(conSeq);
    }

    public String selectContentFileInfoLocal(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return conMulti.selectContentFileInfoLocal(conSeq);
    }


    public String selectMaxSeqInfo() throws Exception {
        // TODO Auto-generated method stub
        return conMulti.selectMaxSeqInfo();
    }

    @Transactional(readOnly = false)
	public int insertContentMutiInfoManage(ContentMutiInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return conMulti.insertContentMutiInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateContentMutiInfoManage(ContentMutiInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return conMulti.updateContentMutiInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteContentMutiInfoManage(String conSeq) throws Exception {
		// TODO Auto-generated method stub
		return conMulti.deleteContentMutiInfoManage(conSeq);
	}

    @Transactional(readOnly = false)
	public int updateContentMutiFile(ContentMutiInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return conMulti.updateContentMutiFile(vo);
	}

    @Transactional(readOnly = false)
	public int updateContentMutiFileLocal(ContentMutiInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return conMulti.updateContentMutiFileLocal(vo);
	}

	


	
	
}
