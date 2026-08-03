package egovframework.let.uat.uia.models;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(title = "LoginReq : 로그인 요청 필드")
public class LoginReq {

	@NotBlank(message = "로그인 아이디를 입력해 주세요.")
	@Schema(description = "로그인 아이디")
	private String userId;

	@NotBlank(message = "로그인 비밀번호를 입력해 주세요.")
	@Schema(description = "로그인 패스워드")
	private String userPwd;
}
