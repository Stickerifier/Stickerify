package com.github.stickerifier.stickerify.exception;

import org.slf4j.helpers.MessageFormatter;

public class MediaException extends Exception {
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
	 * Creates an exception with a parameterized message: each {@code {}}
	 * will be replaced with the corresponding element in {@code parameters}.
	 *
	 * @param message the exception message
	 * @param parameters the parameters to insert into the message
	 * @see MessageFormatter#basicArrayFormat(String, Object[])
	 */
	public MediaException(String message, Object... parameters) {
		this(MessageFormatter.basicArrayFormat(message, parameters));
	}
}
