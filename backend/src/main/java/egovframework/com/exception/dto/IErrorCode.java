package egovframework.com.exception.dto;

import org.springframework.http.HttpStatus;

public interface IErrorCode {

	String name();
	HttpStatus getHttpStatus();
	String getMessage();
}
