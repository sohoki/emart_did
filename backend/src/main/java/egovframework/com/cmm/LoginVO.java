package egovframework.com.cmm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Class Name : LoginVO.java
 * @Description : Login VO class
 * @Modification Information
 * @
 * @  수정일         수정자                   수정내용
 * @ -------    --------    ---------------------------
 * @ 2009.03.03    박지욱          최초 생성
 *
 *  @author 공통서비스 개발팀 박지욱
 *  @since 2009.03.03
 *  @version 1.0
 *  @see
 *  
 */
@Schema(description = "사용자 정보 VO")
@Getter
@Setter
public class LoginVO implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8274004534207618049L;
	
	
	@Schema(description = "사용자 ID", example = "admin123")
	private String managerId;
	
	@Schema(description = "사용자 password", example = "qwe123")
	private String managerPassword;
	
	@Schema(description = "사용자명", example = "qwe123")
	private String managerName;
	
	@Schema(description = "사용유무", example = "qwe123")
	private String useYn;
	
	@Schema(description = "부서아이디")
	private String partId;
	
	@Schema(description = "부서명")
	private String partName;
	
	@Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}")
	@Schema(description = "이메일", example = "qwe123")
	private String managerEmail;
	
	@Schema(description = "연락처", example = "qwe123")
	private String managerTel;
	
	
	@Schema(description = "패스워드 힌트", example = "qwe123")
	private String passwordHint;
	
	@Schema(description = "패스워드 답변", example = "qwe123")
	private String passwordCnsr;
	
	@Schema(description = "잠김여부", example = "qwe123")
	private String lockYn;
	
	@Schema(description = "사번")
	private String empNo;
	
	@Schema(description = "관리자 상태")
	private String managerStatus;
	
	@Schema(description = "권한 아이디", example = "qwe123")
	private String roleId;
	
	@Schema(description = "권한 구분", example = "qwe123")
	private String roleGubun;
	
	@Schema(description = "직급", example = "qwe123")
	private String managerPosition;
	
	@Schema(description = "사진", example = "qwe123")
	private String managerPic;
	
	
	@Schema(description = "거래처 구분", example = "qwe123")
	private String systemcodeUsecode;
	
	@Schema(description = "삭제 여부", example = "qwe123")
	private String dltnYn;
	
	@Schema(description = "접속 IP", example = "192.168.0.1")
	private String ip;

    @Schema(description = "거래처명", example = "거래처")
    private String comName;

    @Schema(description = "거래처명", example = "거래처")
    private String comCode;

    @Schema(description = "거래처 구분", example = "거래처")
    private String comGubun;

    @Schema(description = "거래처 로고", example = "거래처")
    private String comLogo;

    @Schema(description = "지점 정보", example = "지점")
    private String centerId;
}
