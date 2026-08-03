package com.common.backoffice.sts.brd.mapper;

import com.common.backoffice.sts.brd.modals.BasicFileGroupInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface BasicFileGroupInfoManageMapper {
	
	public List<BasicFileGroupInfoVO> selectBasicGroupInfoLst(BasicFileGroupInfoVO searchVO);
	
	public BasicFileGroupInfoVO selectBasicGroupInfoDetail(String groupSeq);
	
	public String selectMaxGroupSeq();
	
	public BasicFileGroupInfoVO selectBasicGroupPreCheck(BasicFileGroupInfoVO searchVO);
	
	public List<BasicFileGroupInfoVO> selectBasicGroupInfoCombo();
	
	public int insertBasicGroupInfo(BasicFileGroupInfoVO vo);
	
	public int updateBasicGroupInfo(BasicFileGroupInfoVO vo);
	
	public int insertBasicGroupCopy(BasicFileGroupInfoVO vo);
	
	public int updateGroupFileCnt(BasicFileGroupInfoVO vo);
	
	public int deleteBasicGroup(String groupSeq);
	
	public int deleteBasicGroupBrodCode(String basicCode);

}
