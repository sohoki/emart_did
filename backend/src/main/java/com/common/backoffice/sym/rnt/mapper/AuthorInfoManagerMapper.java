package com.common.backoffice.sym.rnt.mapper;


import com.common.backoffice.sym.rnt.modals.AuthorInfoVO;
import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import java.util.List;


@Mapper
public interface AuthorInfoManagerMapper {

	public List<AuthorInfoVO> selectAuthorIInfoManageCombo();
	
}