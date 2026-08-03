package com.common.backoffice.bas.cnt.mapper;


import com.common.backoffice.bas.cnt.modals.CenterInfoAnniversary;
import com.common.backoffice.bas.cnt.modals.CenterInfoAnniversaryVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;

@Mapper
public interface CenterAnniManagerMapper {

    List<CenterInfoAnniversaryVO> selectCenterAnniManageListByPagination(CenterInfoAnniversaryVO searchVO);

    CenterInfoAnniversaryVO selectCenterAnniManageDetail(String centerAnniDay);

    int selectCenterAnniManageListTotCnt_S(CenterInfoAnniversaryVO searchVO);

    int selectCenterAnniRetgCheck(CenterInfoAnniversaryVO searchVO);

    int insertCenterAnniManage(CenterInfoAnniversary vo);

    int updateCenterAnniManage(CenterInfoAnniversary vo);

    int deleteCenterAnniManage(String centerAnniDay);

    int deleteCenterID(String centerId);

}
