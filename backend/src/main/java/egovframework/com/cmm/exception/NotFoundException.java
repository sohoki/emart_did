package egovframework.com.cmm.exception;

import lombok.Getter;

@Getter
public class NotFoundException  extends RuntimeException{

	private static final long serialVersionUID = 8588390417360215794L;

	public NotFoundException(String message) {
		super(message);
	}
}
