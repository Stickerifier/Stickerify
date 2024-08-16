package com.github.stickerifier.stickerify.exception;

public class MediaException extends BaseException {
	public MediaException(String message) {
		super(message);
	}

	public MediaException(Throwable cause) {
		super(cause);
	}

	public MediaException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see BaseException#BaseException(String, Object...)
	 */
	public MediaException(String message, Object... parameters) {
		super(message, parameters);
	}
}
