package com.common.backoffice.sts.mhs.mapper;

import com.common.backoffice.sts.mhs.modals.MhsClassInfo;
import com.common.backoffice.sts.mhs.modals.MhsClassInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface MhsClassManageMapper {
	
	public List<MhsClassInfoVO> selectMhsClassList(MhsClassInfoVO searchVO);
	
	public int selectMhsClassListCnt(MhsClassInfoVO searchVO);
	
	public List<MhsClassInfo> selectMhsMoniterClassList(MhsClassInfo vo);
	
	public MhsClassInfo selectMhsClassInfo(String mhsClasscd);

	public String selectMaxMhsClasscd();

	public int insertMhsClassInfo(MhsClassInfo vo);
	
	public int updateMhsClassInfo(MhsClassInfo vo);
	
	public int deleteMhsClassInfo(String mhsClasscd);

}
