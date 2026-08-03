package com.common.backoffice.sts.cnt.service;

import java.util.List;

import com.common.backoffice.sts.cnt.mapper.ContentDetailManagerMapper;
import com.common.backoffice.sts.cnt.modals.ContentDetailInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ContentDetailInfoService {


	private final ContentDetailManagerMapper contentDetail;

	
	public ContentDetailInfo selectContentDetail(String detailSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.selectContentDetail(detailSeq);
	}
    public int selectMaxDetail() throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectMaxDetail();
    }

    public List<ContentDetailInfo> selectContentDetailLst(String conSeq)
            throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectContentDetailLst(conSeq);
    }


    public int selectPageSeqCheck(String conSeq, String detailOrder)
            throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectPageSeqCheck(conSeq, detailOrder);
    }


    public int selectPageSeqCheckPage01(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectPageSeqCheckPage01(conSeq);
    }


    public int selectPageSeqCheckPage02(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectPageSeqCheckPage02(conSeq);
    }

    public int selectPageSeqCheckPage01Cnt(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectPageSeqCheckPage01Cnt(conSeq);
    }


    public int selectPageSeqCheckPage02Cnt(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectPageSeqCheckPage02Cnt(conSeq);
    }


    public List<ContentDetailInfo> selectContentDetailDidPage(ContentDetailInfo searchVO)
            throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectContentDetailDidPage(searchVO);
    }


    public int selectConDetailCnt(String conSeq) throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectConDetailCnt(conSeq);
    }


    public List<ContentDetailInfo> selectConDetailCombo(String conSeq)
            throws Exception {
        // TODO Auto-generated method stub
        return contentDetail.selectConDetailCombo(conSeq);
    }

    @Transactional(readOnly = false)
	public int insertContentDetailManage(ContentDetailInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.insertContentDetailManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateContentDetailManage(ContentDetailInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.updateContentDetailManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteContentDetailManage(String detailSeq) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.deleteContentDetailManage(detailSeq);
	}

    @Transactional(readOnly = false)
	public int deleteContentDetailConSeq(String conSeq) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.deleteContentDetailConSeq(conSeq);
	}

    @Transactional(readOnly = false)
	public int updateContentDetailTimeManage(String detailSeq) throws Exception {
		// TODO Auto-generated method stub
		return contentDetail.updateContentDetailTimeManage(detailSeq);
	}
	
	
}
