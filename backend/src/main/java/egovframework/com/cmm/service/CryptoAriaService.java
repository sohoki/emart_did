package egovframework.com.cmm.service;

import org.apache.commons.codec.binary.Base64;
import org.egovframe.rte.fdl.cryptography.EgovCryptoService;
import org.egovframe.rte.fdl.cryptography.EgovPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class CryptoAriaService {

	@Autowired
	private EgovCryptoService cryptoService;
	
	@Autowired
	private EgovPasswordEncoder passwordEncoder;
	
	public String encryptData(String plainText) {
		String encodeText = null;
		try {

			passwordEncoder.setHashedPassword("385104e5c3df0a68b41441590885486a4e0b9a819c5ae0a395b619a834a71b8e");
			//passwordEncoder.setHashedPassword("hshi");
			byte[] encrypted = cryptoService.encrypt(plainText.getBytes("UTF-8"), "hshi");

			encodeText = Base64.encodeBase64String(encrypted);
		} catch(UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return encodeText;
	}
	
	public String decryptData(String encodeText) {
		String plainText = null;
		try {
			byte[] base64dec = Base64.decodeBase64(encodeText);
			byte[] decrypted = cryptoService.decrypt(base64dec, "hshi");
			plainText = new String(decrypted, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return plainText;
	}
}
