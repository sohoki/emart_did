package com.common.backoffice.bas.uni.service;


import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.commons.lang3.StringUtils;
import com.common.backoffice.sys.log.models.InterfaceInfo;
import com.common.backoffice.sys.log.models.sendEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UtilInfoService {

	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	@Autowired
	protected EgovPropertyService propertiesService;
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public void XMLParse(String xmlData, String[] schemas) throws ParserConfigurationException, SAXException, IOException{
		InputSource is = new InputSource(new StringReader(xmlData));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setAttribute(JAXP_SCHEMA_SOURCE, schemas);
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document doc = documentBuilder.parse(is);
		Element root = doc.getDocumentElement();
		NodeList chideren = root.getChildNodes();
		
		for (int i = 0; i < chideren.getLength(); i ++){
			Node node = chideren.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE){
				Element ele = (Element)node;
				String nodeName = ele.getNodeName();
				if(nodeName.equals("")) {
					
				} else {
					
				}
			}
		}		
	}
	
	/*
	 *  오늘의 날자
	 * 
	 */
	public static String reqDay( int _number  ){
		LocalDate now = LocalDate.now();
		String dayFormat = _number == 0  ? now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) :  now.plusDays(_number).format((DateTimeFormatter.ofPattern("yyyyMMdd")));
		return  dayFormat;
	}
	
	/*
	 *  해당 월 마지막 일자  날 구하기 
	 *  추후 월 일자, 요일 도 같이 하기 
	*/
    public static String reqEndDay(String _day){
    	//String day = LocalDate.parse("20181211", DateTimeFormatter.BASIC_ISO_DATE).with(TemporalAdjusters.firstDayOfMonth()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		String dayFormat = LocalDate.parse(_day, DateTimeFormatter.BASIC_ISO_DATE).with(TemporalAdjusters.lastDayOfMonth()).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		
		return  dayFormat;
	}
    /*
     *  현재 시간과 비교 하여 초 환산 보내 주기 
     */
    public static String timeCheck(String _timeDate) {
    	LocalDateTime now = LocalDateTime.now();
    	//String formatedNow = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		LocalDateTime date = LocalDateTime.parse(_timeDate, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		Duration duration = Duration.between(now, date);
		return  String.valueOf(duration.getSeconds());
    }
    
    /*
     *  현재 시간
     * 
     * 
     */
    public static String nowTime() {
    	LocalDateTime now = LocalDateTime.now();
    	return now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    //null or empty 체크
	public static String NVL(String _val, String _replace) {
		return (_val == null || _val.isEmpty()) ? _replace : _val;
	}
	
	public static String NVL(Object _val, String _replace) {
		return _val == null ? _replace : StringUtils.isBlank(_val.toString()) ? _replace : _val.toString();
	}
	public static String NVLObj(Object _val, String _replace) {
		return _val == null ? _replace : StringUtils.isBlank(_val.toString()) ? _replace : _val.toString();
	}
	public static int NVLObj(Object _val, int _replace) {
		return _val == null ? _replace :  StringUtils.isBlank(_val.toString()) ? _replace :  Integer.parseInt((String.valueOf(_val)));
	}


	public static int NVL(Object _val, int _replace) {
    	return _val == null ? _replace : Integer.valueOf( _val.toString());
    }
	
    public static List<String> dotToList (String _dotlist) {
    	return !_dotlist.equals("") ?  Arrays.asList(_dotlist.split("\\s*,\\s*")) : null;
    }
    
    //요소 확인 후 삭제 하기 
    public static String checkItemList(List<String> _arrayList, String _nowVal, String _newVal) {
    	String itemList = "";
    	if (_arrayList.size()>0) {
    		
    		List list = _arrayList.stream().filter(e ->  !e.startsWith(_nowVal)).collect(Collectors.toList()); 
    		if (!_newVal.isEmpty())
    	    	list.add(_newVal);
    		itemList = (String) list.stream().distinct().sorted().collect(Collectors.joining(","));
    	}
    	return itemList;
    	
    }
    
    /**
     *<pre>
     * 인자로 받은 String이 null일 경우 &quot;&quot;로 리턴한다.
     * &#064;param src null값일 가능성이 있는 String 값.
     * &#064;return 만약 String이 null 값일 경우 &quot;&quot;로 바꾼 String 값.
     *</pre>
     */
    public static String nullConvert(Object src) {
		//if (src != null && src.getClass().getName().equals("java.math.BigDecimal")) {
		if (src != null && src instanceof java.math.BigDecimal) {
		    return ((BigDecimal)src).toString();
		}
	
		if (src == null || src.equals("null")) {
		    return "";
		} else {
		    return ((String)src).trim();
		}
    }
    public static String checkHtmlView(String strString) {
		String strNew = "";

		StringBuffer strTxt = new StringBuffer("");

		char chrBuff;
		int len = strString.length();

		for (int i = 0; i < len; i++) {
			chrBuff = (char) strString.charAt(i);

			switch (chrBuff) {
				case '<':
					strTxt.append("&lt;");
					break;
				case '>':
					strTxt.append("&gt;");
					break;
				case '"':
					strTxt.append("&quot;");
					break;
				case 10:
					strTxt.append("<br>");
					break;
				case ' ':
					strTxt.append("&nbsp;");
					break;
				case '&' :
					strTxt.append("&amp;");
				break;
				default:
					strTxt.append(chrBuff);
			}
		}

		strNew = strTxt.toString();

		return strNew;
	}

    /**
     * Object Empty Check
     * @param obj
     * @return
     */
	public static boolean isEmpty(Object obj) {
		if (obj instanceof String ) return obj == null || "".equals(obj.toString().trim());
		else if (obj instanceof List) return obj == null || ((List)obj).isEmpty();
		else if (obj instanceof Map) return obj == null || ((Map)obj).isEmpty();
		else if (obj instanceof Object[]) return obj == null || Array.getLength(obj) == 0;
		else return obj == null;
		
	}
	

	public static JsonNode safe(JsonNode node, String... path) {
		JsonNode cur = node;
		for (String p : path) {
			if (cur == null) return MissingNode.getInstance(); // 필요 시 import com.fasterxml.jackson.databind.node.MissingNode;
			cur = cur.path(p);
		}
		return cur;
	}

	public static JsonNode getAsJsonNode(String url) throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet get = new HttpGet(url);
			get.addHeader("Accept", "application/json");

			return client.execute(get, response -> {
				int status = response.getCode();
				if (status < 200 || status >= 300) {
					String body = "";
					var entity = response.getEntity();
					if (entity != null) {
						body = EntityUtils.toString(entity, "UTF-8");
					}
					throw new RuntimeException("HTTP GET 실패: status=" + status + ", body=" + body);
				}
				var entity = response.getEntity();
				if (entity == null) {
					throw new RuntimeException("응답 본문이 없습니다.");
				}
				return objectMapper.readTree(EntityUtils.toString(entity, "UTF-8"));
			});
		}
	}


	/**
	 * Http통신 공용 함수
	 * @param _url
	 * @param _sendInfos
	 * @return
	 */
	public JsonNode requestHttpForm(String _url, Map<String, String> _sendInfos) {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost httpPost = new HttpPost(_url);
			List<NameValuePair> nameValuePairs = new ArrayList<>(1);
			_sendInfos.forEach((key, value) -> nameValuePairs.add(new BasicNameValuePair(key, value)));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			return client.execute(httpPost, response -> {
				ObjectMapper objectMapper = new ObjectMapper();
				if (response.getCode() == 200) {
					String body = new BasicHttpClientResponseHandler().handleResponse(response);
					log.error("[RESPONSE] requestHttpForm() " + body);
					return objectMapper.readTree(body);
				} else {
					log.error("response is error:" + response.getCode());
					return objectMapper.readTree("{\"ERROR CODE\":\"" + response.getCode() + "\"}");
				}
			});
		} catch (NullPointerException e) {
			log.error("requestHttpForm IOException requestHttpForm : " + e.toString());
		} catch (IOException e) {
			log.error("requestHttpForm IOException requestHttpForm : " + e.toString());
		}
		return null;
	}

	/**
	 * json 전송후 값 받아 오기   
	 *
	 * @param _url (전송 URL)
	 * @param _jsonInfo (전송 Json)
	 * @param _integId (연계 ID)
	 * @param _provdId (제공 ID)
	 * @param _requstId (요청 ID)
	 * @return
	 */
	public static JsonNode requestHttpJson(String _url, String _jsonInfo, String _integId, String _provdId, String _requstId) {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost httpPost = new HttpPost(_url);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Connection", "keep-alive");
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setEntity(new StringEntity(_jsonInfo, StandardCharsets.UTF_8));
			String requstTrnsmitTm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

			return client.execute(httpPost, response -> {
				ObjectMapper objectMapper = new ObjectMapper();
				InterfaceInfo info = new InterfaceInfo();
				info.setTrsmrcvSeCode(sendEnum.RPQ.getCode());
				info.setIntegId(_integId);
				info.setRequstInsttId(_requstId);
				info.setRspnsRecptnTm(nowTime());

				JsonNode node;
				if (response.getCode() == 200) {
					String body = new BasicHttpClientResponseHandler().handleResponse(response);
					node = objectMapper.readTree(body);
				} else {
					log.error("response is error : " + response.getCode());
					node = objectMapper.readTree("{\"Error_Cd\":\"" + response.getCode() + "\"}");
				}

				info.setRequstTrnsmitTm(requstTrnsmitTm);
				info.setRspnsRecptnTm(nowTime());
				info.setResultCode(node.get("Error_Cd").asText());
				info.setResultMessage(node.toString());
				info.setSendMessage(_jsonInfo);
				info.setProvdInsttId(_provdId);
				info.setRqesterId("SYSTEM");
				return node;
			});
		} catch (NullPointerException e) {
			log.error("encryptPassword NoSuchAlgorithmException ERROR : " + e.toString());
		} catch (Exception e) {
			log.error("requestHttpJson Exception ERROR : " + e.toString());
		}
		return null;
	}
	
	public static String[] getSplitPhNum(String phNum) throws Exception {
		String[] splitPhNumArray = new String[3];
		
		if(phNum.length() >= 9 && phNum.length() <= 12) {
			if(phNum.length() == 11) {
				splitPhNumArray[0] = phNum.substring(0, 3);
				splitPhNumArray[1] = phNum.substring(3, 7);
				splitPhNumArray[2] = phNum.substring(7, 11);
			} else {
				splitPhNumArray[0] = phNum.substring(0, 3);
				splitPhNumArray[1] = phNum.substring(3, 6);
				splitPhNumArray[2] = phNum.substring(6, 10);
			}
		} else {
			log.info("올바르지 않은 휴대전화번호 기입");
			throw new Exception();
		}
		
		return splitPhNumArray;
	}
	
	
	/**
	 * 스피드온 비밀번호 암호화 기능(복호화가 되면 안되므로 SHA-256 인코딩 방식 적용)
	 * 
	 * @param data 암호화할 비밀번호
	 * @param salt Salt
	 * @return 암호화된 비밀번호
	 * @throws Exception
	 */
	public static String encryptPassword(String data, String encryptType) {
		byte[] hashValue = null; // 해쉬값
		MessageDigest md;
		
		try {
			//md = MessageDigest.getInstance(encryptType);
			md = MessageDigest.getInstance("SHA-256");
			SecureRandom ng = new SecureRandom();
			byte[] randomBytes = new byte[16];
			ng.nextBytes(randomBytes);
		
			md.reset();
			md.update(randomBytes);

			hashValue = md.digest(data.getBytes());

		}catch (NoSuchAlgorithmException e) {
			log.error("encryptPassword NoSuchAlgorithmException ERROR : " + e.toString());
		}catch (Exception e) {
			log.error("encryptPassword NoSuchAlgorithmException ERROR : " + e.toString());
		}
		
		return new String(Base64.encodeBase64(hashValue)); 
	}
	
	/**
	 * SPDM제공 관리자 계정 패스워드 암호화 함수
	 * 
	 * @param a_origin
	 * @return
	 */
	public static String getEncryptSHA256(String a_origin) {
		String encryptedSHA256 = "";
		MessageDigest md;
		
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(a_origin.getBytes(), 0, a_origin.length());
			encryptedSHA256 = new BigInteger(1, md.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			log.error("getEncryptSHA256 NoSuchAlgorithmException ERROR : " + e.toString());
		} catch (Exception e) {
			log.error("getEncryptSHA256 NoSuchAlgorithmException ERROR : " + e.toString());
		}
		
		return encryptedSHA256;
	}
	
	/**
	 * 문자열 알파벳만 검증
	 * 
	 * @param String str
	 * @return boolean
	 */
	public static boolean isValidAlphabet(String str) {
		String regex = "^[a-zA-Z]+$";
		// 문자열이 정규식 패턴과 일치하는지 확인
		return str.matches(regex);
	}

	/**
	 * 문자열 이메일 형식 검증
	 * 
	 * @param String email
	 * @return boolean
	 */
	public static boolean isValidEmail(String email) {
		 // 이메일 주소를 검증하기 위한 정규식 패턴
		String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);
		// 정규식에 일치하면 true를 반환, 그렇지 않으면 false를 반환
		return matcher.matches();
	}
}
