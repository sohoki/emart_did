package egovframework.let.uat.uia.service;

import egovframework.com.cmm.LoginVO;
import egovframework.let.uat.uia.mapper.EgovLoginMapper;
import egovframework.let.uat.uia.models.LoginReq;
import egovframework.let.utl.sim.service.EgovFileScrty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일반(관리자) 로그인을 처리하는 비즈니스 구현 클래스.
 * basic/backend(egovframework.let.uat.uia.service.EgovLoginService)를 참조해서
 * did_emart 스키마(tb_managerinfo)에 맞게 포팅함.
 */
@Transactional(value = "txManager", readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class EgovLoginService {

	private final EgovLoginMapper loginMapper;

	/**
	 * 일반 로그인을 처리한다
	 */
	public LoginVO actionLogin(LoginReq vo) throws Exception {
		// 1. 입력한 비밀번호를 암호화한다(salt=아이디).
		//String enpassword = EgovFileScrty.encryptPassword(vo.getUserPwd(), vo.getUserId());
		//vo.setUserPwd(enpassword);

		// 2. 아이디와 암호화된 비밀번호가 DB와 일치하는지 확인한다.
		LoginVO loginVO = loginMapper.actionLogin(vo);

		if (loginVO != null && loginVO.getManagerId() != null && !loginVO.getManagerId().equals("")) {
			return loginVO;
		}
		return new LoginVO();
	}

	/**
	 * Refresh Token으로 재로그인 처리한다
	 */
	public LoginVO actionLoginResfresh(String resfreshToken) throws Exception {
		return loginMapper.actionLoginResfresh(resfreshToken);
	}
}
