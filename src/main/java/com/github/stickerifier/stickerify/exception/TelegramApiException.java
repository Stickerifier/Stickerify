package com.github.stickerifier.stickerify.exception;

public class TelegramApiException extends BaseException {
	/**
	 * @see BaseException#BaseException(String, Object...)
	 */
	public TelegramApiException(String message, Object... parameters) {
		super(message, parameters);
	}
}
