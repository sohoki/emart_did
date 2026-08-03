package com.common.backoffice.sts.pic.service;


import java.util.List;


import javax.annotation.Resource;

import com.common.backoffice.sts.pic.mapper.DidMoniterPicManagerMapper;
import com.common.backoffice.sts.pic.modals.DidMoniterPic;
import com.common.backoffice.sts.pic.modals.DidMoniterPicVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class DidMoniterPicService {

	private final DidMoniterPicManagerMapper didMoniterMapper;
	
	
	public List<DidMoniterPicVO> selectDidMoniterPicManageListByPagination(
			DidMoniterPicVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return didMoniterMapper.selectDidMoniterPicManageListByPagination(searchVO);
	}

	
	public int selectDidMoniterPicManageListTotCnt_S(DidMoniterPicVO searchVO)
			throws Exception {
		// TODO Auto-generated method stub
		return didMoniterMapper.selectDidMoniterPicManageListTotCnt_S(searchVO);
	}

    @Transactional(readOnly = false)
	public int insertDidMoniterPicManage(DidMoniterPic vo) throws Exception {
		// TODO Auto-generated method stub
		return didMoniterMapper.insertDidMoniterPicManage(vo);
	}

	
	
}
