package com.common.backoffice.sts.brd.service;

import java.util.List;

import javax.annotation.Resource;

import com.common.backoffice.sts.brd.mapper.BasicFileGroupPlayInfoManageMapper;
import com.common.backoffice.sts.brd.modals.BasicFileGroupPlayInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BasicFileGroupPlayInfoManageService  {
	
	private final BasicFileGroupPlayInfoManageMapper playMapper;

    public List<BasicFileGroupPlayInfoVO> selectPlayListInfo(BasicFileGroupPlayInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return playMapper.selectPlayListInfo(searchVO);
    }


    public List<BasicFileGroupPlayInfoVO> selectPlayListInfoNotCenter(BasicFileGroupPlayInfoVO searchVO) throws Exception {
        // TODO Auto-generated method stub
        return playMapper.selectPlayListInfoNotCenter(searchVO);
    }

    @Transactional(readOnly = false)
	public int updateFilePlay(BasicFileGroupPlayInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		int cnt = playMapper.selectFileInserCheck(vo);
		int ret = 0;
		
		if (cnt > 0 ){
			ret = playMapper.updateFilePlay(vo);
		}else {
			ret = playMapper.insertFilePlay(vo);
		}
		return ret;
	}

    @Transactional(readOnly = false)
	public int deleteFilePlay(BasicFileGroupPlayInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		return playMapper.deleteFilePlay(vo);
	}

	

}
