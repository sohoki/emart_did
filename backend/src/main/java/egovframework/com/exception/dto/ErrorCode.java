package egovframework.com.exception.dto;

public enum ErrorCode {

	INVALID_INPUT_VALUE(400, "E001", "err.invalid.input.value"), // Bad Request
	INVALID_TYPE_VALUE(400, "E002", "err.invalid.type.value"), // Bad Request
	ENTITY_NOT_FOUND(400, "E003", "err.entity.not.found"), // Bad Request
	UNAUTHORIZED(401, "E004", "err.unauthorized"), // The request requires an user authentication
	ACCESS_DENIED(403, "E005", "err.access.denied"), // Forbidden, Access is Denied
	NOT_FOUND(404, "E007", "err.not.found"), // Not found
	METHOD_NOT_ALLOWED(405, "E008", "err.method.not.allowed"), // 요청 방법이 서버에 의해 알려졌으나, 사용 불가능한 상태
	UNPROCESSABLE_ENTITY(422, "E009", "err.unprocessable.entity"), // Unprocessable Entity
	INTERNAL_SERVER_ERROR(500, "E999", "err.internal.server"), // Server Error
	EXPIRED_JWT(400, "E011", "JWT 토큰 만료"), // JWT Unavailable
	INVALID_JWT(400, "E012", "JWT 잘못된 토큰"), // JWT Unavailable
	REDIS_ERROR(500, "E020", "REDIS ERROR"), // JWT Unavailable
	
	SERVICE_UNAVAILABLE(503, "E010", "err.service.unavailable") // Service Unavailable
	;
		
	private final int status;
	private final String code;
	private final String message;
	
	ErrorCode(final int status, final String code, final String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
}
