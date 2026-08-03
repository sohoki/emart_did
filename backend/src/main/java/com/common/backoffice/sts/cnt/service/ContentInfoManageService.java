package com.common.backoffice.sts.cnt.service;

import java.util.List;

import javax.annotation.Resource;

import com.common.backoffice.sts.cnt.mapper.ContentInfoManagerMapper;
import com.common.backoffice.sts.cnt.modals.ContentInfo;
import com.common.backoffice.sts.cnt.modals.ContentInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class ContentInfoManageService  {

	
	private final ContentInfoManagerMapper contentInfoMapper;
	
	
	
	public int selectContentInfoManageListTotCnt_S( ContentInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return contentInfoMapper.selectContentInfoManageListTotCnt_S(searchVO);
	}

	
	public List<ContentInfoVO>  selectContentInfoManageListByPagination(ContentInfoVO SearchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return contentInfoMapper.selectContentInfoManageListByPagination(SearchVO);
	}

	
	public ContentInfoVO selectContentInfoManageDetail(String conSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return contentInfoMapper.selectContentInfoManageDetail(conSeq);
	}

    public List<ContentInfo> selectNextCombo(ContentInfoVO vo) throws Exception {
        // TODO Auto-generated method stub
        return contentInfoMapper.selectNextCombo(vo);
    }


    public List<ContentInfo> selectSearcHCombo(ContentInfoVO vo)
            throws Exception {
        // TODO Auto-generated method stub
        return contentInfoMapper.selectSearcHCombo(vo);
    }

    @Transactional(readOnly = false)
	public int insertContentInfoManage(ContentInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return contentInfoMapper.insertContentInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int updateContentInfoManage(ContentInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return contentInfoMapper.updateContentInfoManage(vo);
	}

    @Transactional(readOnly = false)
	public int deleteContentInfoManage(String conSeq) throws Exception {
		// TODO Auto-generated method stub
		return contentInfoMapper.deleteContentInfoManage(conSeq);
	}


	
	
	
	

}
