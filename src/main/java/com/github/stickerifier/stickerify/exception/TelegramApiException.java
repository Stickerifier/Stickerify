package com.github.stickerifier.stickerify.exception;

import org.slf4j.helpers.MessageFormatter;

public class TelegramApiException extends Exception {
	/**
	 * Creates an exception with a parameterized message: each {@code {}}
	 * will be replaced with the corresponding element in {@code parameters}.
	 *
	 * @param message the exception message
	 * @param parameters the parameters to insert into the message
	 * @see MessageFormatter#basicArrayFormat(String, Object[])
	 */
	public TelegramApiException(String message, Object... parameters) {
		this(MessageFormatter.basicArrayFormat(message, parameters));
	}

	private TelegramApiException(String message) {
		super(message);
	}
}
