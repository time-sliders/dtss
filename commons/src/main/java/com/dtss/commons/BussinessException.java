package com.dtss.commons;

/**
 * 业务异常类,
 * 必须填写业务的错误信息.
 * 错误信息是可以直接给用户看的.
 */
public class BussinessException extends Exception {

	private static final long serialVersionUID = -3591382539768191631L;

	/**
	 * 业务异常编码，可以给出对应的异常编码列表时，建议使用。
	 *
	 */
	private String code;

	public BussinessException(String message) {
		super(message);
	}

	public BussinessException(String code, String message) {
		super(message);
		this.code = code;
	}


	public BussinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BussinessException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
