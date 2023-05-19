package com.github.stickerifier.stickerify.telegram.exception;

import java.util.regex.Pattern;

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
		this(fillWithParameters(message, parameters));
	}

	private static String fillWithParameters(final String message, final Object... parameters) {
		String formattedMessage = message;
		for (var parameter : parameters) {
			formattedMessage = formattedMessage.replaceFirst(Pattern.quote("{}"), String.valueOf(parameter));
		}

		return formattedMessage;
	}
}
