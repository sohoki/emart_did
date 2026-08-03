package com.common.backoffice.sts.brd.mapper;

import com.common.backoffice.sts.brd.modals.BrodOrganization;
import com.common.backoffice.sts.brd.modals.BrodOrganizationVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface BrodOrganizationManagerMapper {
	
	
	public List<BrodOrganizationVO> selectBrodOrganizationLst(BrodOrganizationVO searchVO);
	
	public BrodOrganizationVO selectBrodOrganizationInfo(String orgSeq);
	
	public List<BrodOrganization> selectBrodOrgnizationPage(BrodOrganization  vo);
	
	public List<BrodOrganization> selectBrodOrgnizationDid(BrodOrganization vo);
	
	public int selectOrganizationPageCnt(BrodOrganizationVO searchVO);
	
	public int insertBrodOrganization(BrodOrganization vo);
	
	public int updateBrodOrganization(BrodOrganization vo);
	
	public int deleteBrodOrganization(String brodCode);
	
	public int deleteBrodOrganizationCenterId(BrodOrganization vo);
	
	public int deleteContentToOrg(String brodCode);

}
