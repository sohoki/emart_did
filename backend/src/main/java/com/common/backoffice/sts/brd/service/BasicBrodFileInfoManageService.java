package com.common.backoffice.sts.brd.service;

import java.util.List;

import com.common.backoffice.sts.brd.mapper.BasicBrodFileManagerMapper;
import com.common.backoffice.sts.brd.modals.BasicBrodFileInfo;
import com.common.backoffice.sts.brd.modals.BasicBrodFileInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BasicBrodFileInfoManageService {

	private final BasicBrodFileManagerMapper basicMapper;

	
	public List<BasicBrodFileInfoVO> selectBasicBrodFileLst(String basicCode)
			throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.selectBasicBrodFileLst(basicCode);
	}
	
	
	public List<BasicBrodFileInfoVO> selectBasicBrodSchFileLst(BasicBrodFileInfoVO vo)
			throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.selectBasicBrodSchFileLst(vo);
	}
	
	
	public List<BasicBrodFileInfoVO> selectBasicBrodSchFileLstNew(String basicCode)
			throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.selectBasicBrodSchFileLstNew(basicCode);
	}

    @Transactional(readOnly = false)
	public int insertBasicBrodFile(BasicBrodFileInfo vo) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.insertBasicBrodFile(vo);
	}

    @Transactional(readOnly = false)
	public int insertBasicBrodFileCopy(BasicBrodFileInfoVO vo) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.insertBasicBrodFileCopy(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBasicBrodFile(String basicSeq) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.deleteBasicBrodFile(basicSeq);
	}

    @Transactional(readOnly = false)
	public int deleteBasicBrodBasicCode(String basicCode) throws Exception {
		// TODO Auto-generated method stub
		return basicMapper.deleteBasicBrodBasicCode(basicCode);
	}
}
