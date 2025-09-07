package com.github.stickerifier.stickerify.exception;

public class TelegramApiException extends BaseException {
	private final String description;

	public TelegramApiException(String requestMethod, String description) {
		super("Telegram couldn't execute the {} request: {}", requestMethod, description);
		this.description = description;
	}

	/**
	 * @return the description of the error received by the api call
	 */
	public String getDescription() {
		return description;
	}
}
