package com.common.backoffice.sts.brd.service;


import java.util.List;
import javax.annotation.Resource;

import com.common.backoffice.sts.brd.mapper.BrodAnniversaryManagerMapper;
import com.common.backoffice.sts.brd.modals.BrodAnniversary;
import com.common.backoffice.sts.brd.modals.BrodAnniversaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class BrodAnniversaryManagerService {
	
	private final BrodAnniversaryManagerMapper anniMapper;
	
	

	
	public List<BrodAnniversaryVO> selectBrodAnniverLst(BrodAnniversary vo) throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.selectBrodAnniverLst(vo);
	}

	
	public BrodAnniversaryVO selectBrodAnniver(String brodAnnSeq)
			throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.selectBrodAnniver(brodAnnSeq);
	}

	
	public int selectBrodAnniverPageCnt(String brodCode)
			throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.selectBrodAnniverPageCnt(brodCode);
	}

    public String selectBrodAnnMaxSeq() throws Exception {
        // TODO Auto-generated method stub
        return anniMapper.selectBrodAnnMaxSeq();
    }

    @Transactional(readOnly = false)
	public int insertBrodAnniver(BrodAnniversary vo) throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.insertBrodAnniver(vo);
	}
    @Transactional(readOnly = false)
	public int insertBrodAnniverCopy(BrodAnniversary vo) throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.insertBrodAnniverCopy(vo);
	}

    @Transactional(readOnly = false)
	public int updateBrodAnniver(BrodAnniversary vo) throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.updateBrodAnniver(vo);
	}

    @Transactional(readOnly = false)
	public int deleteBrodAnniver(String brodAnnSeq) throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.deleteBrodAnniver(brodAnnSeq);
	}

    @Transactional(readOnly = false)
	public int deleteBrodAnniverBrod(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.deleteBrodAnniverBrod(brodCode);
	}

    @Transactional(readOnly = false)
	public int insertBrodAnniverCenterCopy(BrodAnniversary vo) throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.insertBrodAnniverCenterCopy(vo);
	}

	


    @Transactional(readOnly = false)
	public int deleteBrodAnnBasicBrod(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.deleteBrodAnnBasicBrod(brodCode);
	}

    @Transactional(readOnly = false)
	public int insertBrodAnniverBasicBrodCodeCopy(String brodCode)
			throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.insertBrodAnniverBasicBrodCodeCopy(brodCode);
	}

    @Transactional(readOnly = false)
	public int deleteBrodAnniverBrodAll(String brodCode) throws Exception {
		// TODO Auto-generated method stub
		return anniMapper.deleteBrodAnniverBrodAll(brodCode);
	}

}
