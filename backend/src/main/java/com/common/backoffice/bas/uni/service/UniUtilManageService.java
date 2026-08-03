package com.common.backoffice.bas.uni.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.backoffice.bas.uni.mapper.UniUtilManageMapper;
import com.common.backoffice.bas.uni.models.UniUtilInfo;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UniUtilManageService {

	private final UniUtilManageMapper uniMapper;
	
	public int selectIdDoubleCheck(UniUtilInfo vo) throws Exception {
		
		return uniMapper.selectIdDoubleCheck(vo);
	}
	
	
	public int selectIdDoubleCheckString(String _field, String _tableNm, String _condition ) throws Exception {
		
		UniUtilInfo vo = new UniUtilInfo();
		vo.setInCheckName(_field);
		vo.setInTable(_tableNm);
		vo.setInCondition(_condition);
		
		return uniMapper.selectIdDoubleCheck(vo);
	}

	@Transactional(readOnly = false)
	public int deleteUniStatement(UniUtilInfo vo) throws Exception {
		
		int ret = uniMapper.deleteUniStatement(vo);
		return 1;
	}

	
	public String selectMaxValue(UniUtilInfo vo) throws Exception {
		
		return uniMapper.selectMaxValue(vo);
	}
}
