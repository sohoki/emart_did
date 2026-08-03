package com.common.backoffice.sts.brd.mapper;

import com.common.backoffice.sts.brd.modals.BrodAnniversary;
import com.common.backoffice.sts.brd.modals.BrodAnniversaryVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;


@Mapper
public interface BrodAnniversaryManagerMapper {
	
	public List<BrodAnniversaryVO> selectBrodAnniverLst(BrodAnniversary vo);
	
	public BrodAnniversaryVO selectBrodAnniver(String brodAnnSeq);
	
	public String selectBrodAnnMaxSeq();
	
	public int selectBrodAnniverPageCnt(String brodCode);
	
	public int insertBrodAnniver(BrodAnniversary vo);
	
	public int insertBrodAnniverCopy(BrodAnniversary vo);
	
	public int insertBrodAnniverCenterCopy(BrodAnniversary vo);
	
	public int insertBrodAnniverBasicBrodCodeCopy(String brodCode);
	
	public int updateBrodAnniver(BrodAnniversary vo);
	
	public int deleteBrodAnniver(String brodAnnSeq);
	
	public int deleteBrodAnniverBrod (String brodCode);
	
	public int deleteBrodAnniverBrodAll(String brodCode);
	
	public int deleteBrodAnnBasicBrod(String brodCode);
}
