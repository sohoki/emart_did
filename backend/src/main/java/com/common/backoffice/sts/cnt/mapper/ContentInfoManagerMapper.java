package com.common.backoffice.sts.cnt.mapper;

import java.util.List;

import com.common.backoffice.sts.cnt.modals.ContentInfo;
import com.common.backoffice.sts.cnt.modals.ContentInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;


@Mapper
public interface ContentInfoManagerMapper {


    public int   selectContentInfoManageListTotCnt_S(ContentInfoVO SearchVO);

    public List<ContentInfoVO>   selectContentInfoManageListByPagination(ContentInfoVO SearchVO);

    public List<ContentInfo> selectNextCombo (ContentInfoVO vo);

    public List<ContentInfo> selectSearcHCombo (ContentInfoVO vo);

    public ContentInfoVO selectContentInfoManageDetail(String conSeq);

    public int insertContentInfoManage(ContentInfo vo);

    public int updateContentInfoManage(ContentInfo vo);

    public int deleteContentInfoManage (String conSeq);




}
