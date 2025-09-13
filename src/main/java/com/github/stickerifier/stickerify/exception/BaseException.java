package com.github.stickerifier.stickerify.exception;

import org.slf4j.helpers.MessageFormatter;

public class BaseException extends Exception {
	public BaseException(String message) {
		super(message);
	}

	public BaseException(Throwable cause) {
		super(cause);
	}

	public BaseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates an exception with a parameterized message: each {@code {}}
	 * will be replaced with the corresponding element in {@code parameters}.
	 *
	 * @param message the exception message
	 * @param parameters the parameters to insert into the message
	 * @see MessageFormatter#basicArrayFormat(String, Object[])
	 */
	public BaseException(String message, Object... parameters) {
		super(MessageFormatter.basicArrayFormat(message, parameters));
	}
}
