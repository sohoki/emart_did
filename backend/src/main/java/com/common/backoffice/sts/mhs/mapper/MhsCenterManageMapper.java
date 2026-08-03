package com.common.backoffice.sts.mhs.mapper;

import com.common.backoffice.bas.cnt.modals.CenterInfo;
import com.common.backoffice.bas.cnt.modals.CenterInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface MhsCenterManageMapper {
	
	public List<CenterInfoVO> selectMhsBrandList(CenterInfoVO mhsCenterInfoVO);
	//상위 부서 콤보 박스
	public List<CenterInfoVO> selectMhsComboList(String mhsBrandcd);
	
	public List<CenterInfoVO> selectMhsComboListMeber(String  mhsBrandcd);
	
	public CenterInfoVO selectMhsCenterInfo (String mhsCentercd);
	
	public List<CenterInfoVO> selectMhsCenterList(CenterInfoVO mhsCenterInfoVO);

	public int insertMhsCenter (CenterInfo vo);
	
	public int updateMhsCenter (CenterInfo vo);
	
	public int deleteMhsCenter (String  mhsCentercd);
}
