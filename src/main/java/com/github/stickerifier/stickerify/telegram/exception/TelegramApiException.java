package com.github.stickerifier.stickerify.telegram.exception;

import org.slf4j.helpers.MessageFormatter;

public final class TelegramApiException extends Exception {
	public TelegramApiException(String message) {
		super(message);
	}

	public TelegramApiException(Throwable cause) {
		super(cause);
	}

	public TelegramApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public TelegramApiException(String message, Object... parameters) {
		this(MessageFormatter.basicArrayFormat(message, parameters));
	}
}
