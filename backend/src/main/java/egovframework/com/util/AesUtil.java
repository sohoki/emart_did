package egovframework.com.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AesUtil {

	// 16,24,34 byte
	private static String key  = "COMM_INFOqwer123";


	public static String aesCBCEEnode(String painText) throws Exception{

		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"),"AES");
			IvParameterSpec IV = new IvParameterSpec(key.substring(0,16).getBytes());
			Cipher c =  Cipher.getInstance("AES/CBC/PKCS5Padding");

			c.init(Cipher.ENCRYPT_MODE, secretKey, IV);

			byte[] encryptionByte = c.doFinal(painText.getBytes());

			return Hex.encodeHexString(encryptionByte);
		}catch(Exception e) {
			log.error("ex:" + e.toString());
			return "";
		}


	}
	public static String aesCBCDecode(String encodeText) throws Exception{
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"),"AES");
			IvParameterSpec IV = new IvParameterSpec(key.substring(0,16).getBytes());

			Cipher c =  Cipher.getInstance("AES/CBC/PKCS5Padding");
			c.init(Cipher.DECRYPT_MODE, secretKey, IV);

			byte[] decodeByte = Hex.decodeHex(encodeText.toCharArray());
			return new String(c.doFinal(decodeByte), "UTF-8");
		}catch(Exception e) {
			log.error("ex:" + e.toString());
			return "";
		}

	}
}
