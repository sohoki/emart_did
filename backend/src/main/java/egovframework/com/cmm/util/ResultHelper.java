package egovframework.com.cmm.util;


import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.Globals;
import egovframework.com.cmm.service.ResultVO;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ResultHelper {

	private ResultHelper() {}
	 
	/**
	 * 성공 응답을 세팅한다.
	 */
	public static void setSuccess(ResultVO resultVO) {
		resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
		resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
	}
	

	
	/**
	 * 성공 응답 + 결과 데이터를 세팅한다.
	 */
	public static void setSuccess(ResultVO resultVO, Object data, String resultMsg) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(resultMsg, data);
		
		setSuccess(resultVO);
		resultVO.setResult(resultMap);
	}
	/**
	 * 성공 응답 + 페이징 결과 데이터를 세팅한다.
	 */
	public static void setSuccess(ResultVO resultVO, Map<String, Object> resultMap) {
		setSuccess(resultVO);
		resultVO.setResult(resultMap);
	}
	
	public static void setSuccess(ResultVO resultVO, String successMsgKey, EgovMessageSource messageSource) {
		resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
		resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
		resultVO.setResultMessage(messageSource.getMessage(successMsgKey));
	}
	
	/**
	 * CUD 작업 결과를 세팅한다.
	 *
	 * @param resultVO      결과 객체
	 * @param affectedRows  영향받은 행 수
	 * @param successMsgKey 성공 시 메시지 키 (예: "success.common.delete")
	 * @param messageSource 메시지 소스
	 */
	public static void setCudResult(ResultVO resultVO, int affectedRows,
									String successMsgKey, EgovMessageSource messageSource) {
		if (affectedRows > 0) {
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			resultVO.setResultMessage(messageSource.getMessage(successMsgKey));
		} else {
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			resultVO.setResultMessage(messageSource.getMessage("fail.request.msg"));
		}
	}
	public static void setCudResult(ResultVO resultVO, int affectedRows,
				String successMsgKey, String failMsgKey, EgovMessageSource messageSource) {
		if (affectedRows > 0) {
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			resultVO.setResultMessage(messageSource.getMessage(successMsgKey));
		} else {
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
			resultVO.setResultMessage(messageSource.getMessage(failMsgKey));
		}
	}
	
	public static void setCudResult(ResultVO resultVO, int resCode, String stateCode, 
									EgovMessageSource messageSource, 
									String message) {
		
		resultVO.setResultCode(resCode);
		resultVO.setResultCodeInfo(stateCode);
		resultVO.setResultMessage(messageSource.getMessage(message));
		
	}
	public static void setCudMsgResult(ResultVO resultVO, int affectedRows,
			String message, EgovMessageSource messageSource) {
		if (affectedRows > 0) {
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_SUCCESS);
			
		} else {
			resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
			resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
		}
		resultVO.setResultMessage(messageSource.getMessage(message));
	}
	public static void setCudResult(ResultVO resultVO, int resCode, String stateCode, String message) {
		resultVO.setResultCode(resCode);
		resultVO.setResultCodeInfo(stateCode);
		resultVO.setResultMessage(message);
	
	}
	
	/**
	 * 에러 응답을 세팅하고 로깅한다.
	 * NullPointerException은 debug, 그 외는 error 레벨.
	 *
	 * @param resultVO      결과 객체
	 * @param methodName    호출 메서드명 (로깅용)
	 * @param e             발생한 예외
	 * @param messageSource 메시지 소스
	 */
	public static void setFailResult(ResultVO resultVO, 
									String methodName,
									Exception e, 
									EgovMessageSource messageSource) {
		if (e instanceof NullPointerException) {
			log.error("{} error: {}", methodName, e.toString());
		} else {
			log.error("{} error: {}", methodName, e.toString());
		}
		resultVO.setResultCode(ResponseCode.SERVER_ERROR.getCode());
		resultVO.setResultCodeInfo(Globals.STATUS_FAIL);
		resultVO.setResultMessage(messageSource.getMessage("fail.common.msg"));
	}
	public static void setFailAuthResult(ResultVO resultVO) {
		resultVO.setResultCode(ResponseCode.NO_AUTH.getCode());
		resultVO.setResultMessage(ResponseCode.NO_AUTH.getMessage());
	}
}
