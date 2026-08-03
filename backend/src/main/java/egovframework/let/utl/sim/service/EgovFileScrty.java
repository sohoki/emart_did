package egovframework.let.utl.sim.service;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

/**
 * 비밀번호를 SHA-256 해시로 암호화하는 유틸리티 클래스.
 * basic/backend(egovframework.let.utl.sim.service.EgovFileScrty)를 참조해서
 * did_emart에는 로그인에 필요한 비밀번호 암호화 기능만 포팅함.
 */
public class EgovFileScrty {

	/**
	 * 비밀번호를 암호화하는 기능(복호화가 되면 안되므로 SHA-256 인코딩 방식 적용, salt로 ID 지정)
	 *
	 * @param password 암호화될 패스워드
	 * @param id salt로 사용될 사용자 ID
	 * @return 암호화된 비밀번호(Base64)
	 * @throws Exception
	 */
	public static String encryptPassword(String password, String id) throws Exception {

		if (password == null) {
			return "";
		}

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.reset();
		md.update(id.getBytes());

		byte[] hashValue = md.digest(password.getBytes());

		return new String(Base64.encodeBase64(hashValue));
	}
}
