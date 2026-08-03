package com.common.backoffice.sts.xml.mapper;

import com.common.backoffice.sts.xml.modals.XmlInfo;
import com.common.backoffice.sts.xml.modals.XmlInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;



@Mapper
public interface XmlInfoManagerMapper {

	
	public List<XmlInfoVO> selectXmlInfoManageListByPagination(XmlInfoVO searchVO);
	
	public XmlInfoVO selectXmlrInfoManageDetail(String xmlSeq);
	
	public XmlInfoVO selectXmlrInfoManageNameDetail(String xmlProcessName);
	
	public List<XmlInfo> selectXmlProcessCombo();
	
	public int selectXmlInfoManageListTotCnt_S(XmlInfoVO searchVO);
	
	public int insertXmlInfoManage(  XmlInfo vo);
	
	public int updateXmlInfoManage(  XmlInfo vo);
	
	public int deleteXmlInfoManage(String xmlSeq);
	
	public int selectXmlProcessCount(String xmlProcessName);

	public String selectDIDProcessNm(String code);

}
