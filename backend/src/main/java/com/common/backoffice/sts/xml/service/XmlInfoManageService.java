package com.common.backoffice.sts.xml.service;

import java.util.List;

import javax.annotation.Resource;

import com.common.backoffice.sts.xml.mapper.XmlInfoManagerMapper;
import com.common.backoffice.sts.xml.modals.XmlInfo;
import com.common.backoffice.sts.xml.modals.XmlInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class XmlInfoManageService {

	
	private final XmlInfoManagerMapper xmlInfoManagerMapper;
	
	
	public List<XmlInfoVO> selectXmlInfoManageListByPagination( XmlInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return xmlInfoManagerMapper.selectXmlInfoManageListByPagination(searchVO);
	}

	
	public XmlInfoVO selectXmlrInfoManageDetail(String xmlSeq) throws Exception {
		// TODO Auto-generated method stub
		return xmlInfoManagerMapper.selectXmlrInfoManageDetail(xmlSeq);
	}

	
	public int selectXmlInfoManageListTotCnt_S(XmlInfoVO searchVO) throws Exception {
		// TODO Auto-generated method stub
		return xmlInfoManagerMapper.selectXmlInfoManageListTotCnt_S(searchVO);
	}

	

	
	public int selectXmlProcessCount(String xmlProcessName) throws Exception {
		// TODO Auto-generated method stub
		return xmlInfoManagerMapper.selectXmlProcessCount(xmlProcessName);
	}

	
	public XmlInfoVO selectXmlrInfoManageNameDetail(String xmlProcessName)
			throws Exception {
		// TODO Auto-generated method stub
		return xmlInfoManagerMapper.selectXmlrInfoManageNameDetail(xmlProcessName);
	}

	
	public String selectDIDProcessNm(String code) throws Exception {
		// TODO Auto-generated method stub
		return xmlInfoManagerMapper.selectDIDProcessNm(code);
	}

	
	public List<XmlInfo> selectXmlProcessCombo() throws Exception {
		// TODO Auto-generated method stub
		return xmlInfoManagerMapper.selectXmlProcessCombo();
	}

    @Transactional(readOnly = false)
    public int insertXmlInfoManage(XmlInfo vo) throws Exception {
        return xmlInfoManagerMapper.insertXmlInfoManage(vo);
    }

    @Transactional(readOnly = false)
    public int updateXmlInfoManage(XmlInfo vo) throws Exception {
        // TODO Auto-generated method stub
        return xmlInfoManagerMapper.updateXmlInfoManage(vo);
    }

    @Transactional(readOnly = false)
    public int deleteXmlInfoManage(String xmlSeq) throws Exception {
        // TODO Auto-generated method stub
        return xmlInfoManagerMapper.deleteXmlInfoManage(xmlSeq);
    }

}
