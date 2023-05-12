package com.github.stickerifier.stickerify.telegram.exception;

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
}
