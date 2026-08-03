package com.common.backoffice.bas.cnt.mapper;


import com.common.backoffice.bas.cnt.modals.CenterInfo;
import com.common.backoffice.bas.cnt.modals.CenterInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface CenterInfoManagerMapper {

    public List<CenterInfoVO> selectCenterInfoManageListByPagination(CenterInfoVO vo);

    public List<CenterInfoVO> selectCenterInfoManageCombo(CenterInfoVO vo);

    public CenterInfoVO selectCenterInfoManageDetail(String centerId);

    public List<CenterInfo> selectCenterBrodCombo(String centerId);

    public String selectCenterTimeInfo(CenterInfo vo);

    public String selectCenterInfoBrod(String centerId);

    public String selectCenterId();

    public int selectCenterInfoManageListTotCnt_S(CenterInfoVO vo);

    public int insertCenterInfoManage(CenterInfo vo);

    public int updateCenterInfoManage(CenterInfo vo);

    public int deleteCenterInfoManage (String centerId);

    public List<CenterInfoVO> selectGroupInCenterInfo(CenterInfoVO vo);
}
