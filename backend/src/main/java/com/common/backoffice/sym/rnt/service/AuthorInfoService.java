package com.common.backoffice.sym.rnt.service;


import java.util.List;

import javax.annotation.Resource;

import com.common.backoffice.sym.rnt.mapper.AuthorInfoManagerMapper;
import com.common.backoffice.sym.rnt.modals.AuthorInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class AuthorInfoService{


	private final AuthorInfoManagerMapper authorInfoManagerMapper;

	public List<AuthorInfoVO> selectAuthorIInfoManageCombo() {
		// TODO Auto-generated method stub
		return authorInfoManagerMapper.selectAuthorIInfoManageCombo();
	}

	
	
}
