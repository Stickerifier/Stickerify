package com.github.stickerifier.stickerify.exception;

import org.slf4j.helpers.MessageFormatter;

public class BaseException extends Exception {
	public BaseException(Throwable cause) {
		super(cause);
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
