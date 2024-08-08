package com.github.stickerifier.stickerify.exception;

public class ProcessException extends BaseException {
	public ProcessException(Throwable cause) {
		super(cause);
	}

	/**
	 * @see BaseException#BaseException(String, Object...)
	 */
	public ProcessException(String message, Object... parameters) {
		super(message, parameters);
	}
}
