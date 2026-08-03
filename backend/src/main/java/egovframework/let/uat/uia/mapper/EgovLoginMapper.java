package egovframework.let.uat.uia.mapper;

import org.egovframe.rte.psl.dataaccess.mapper.Mapper;

import egovframework.com.cmm.LoginVO;
import egovframework.let.uat.uia.models.LoginReq;

@Mapper
public interface EgovLoginMapper {

	public LoginVO actionLogin(LoginReq vo);

	public LoginVO actionLoginResfresh(String resfreshToken);
}
