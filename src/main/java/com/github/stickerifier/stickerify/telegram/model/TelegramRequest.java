package com.github.stickerifier.stickerify.telegram.model;

import static com.github.stickerifier.stickerify.telegram.Answer.ABOUT;
import static com.github.stickerifier.stickerify.telegram.Answer.HELP;
import static java.util.Comparator.comparing;

import com.github.stickerifier.stickerify.telegram.Answer;
import com.github.stickerifier.stickerify.telegram.exception.TelegramApiException;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;

import java.util.Arrays;
import java.util.Optional;

/**
 * Data class wrapping a {@link Message} instance to represent a Telegram request.
 *
 * @param message the message to wrap
 */
public record TelegramRequest(Message message) {

	public static final String NEW_USER = " (new user)";
	private static final String START_COMMAND = "/start";
	private static final String HELP_COMMAND = "/help";

	public boolean hasFile() {
		return message.photo() != null || message.document() != null || message.sticker() != null
				|| message.video() != null || message.videoNote() != null
				|| message.audio() != null || message.voice() != null;
	}

	public String getFileId() throws TelegramApiException {
		if (message.photo() != null) {
			return Arrays.stream(message.photo()).max(comparing(PhotoSize::fileSize)).orElseThrow().fileId();
		} else if (message.document() != null) {
			return message.document().fileId();
		} else if (message.sticker() != null) {
			return message.sticker().fileId();
		} else if (message.video() != null) {
			return message.video().fileId();
		} else if (message.videoNote() != null) {
			return message.videoNote().fileId();
		}

		throw new TelegramApiException("The request doesn't contain a supported media: {}", message);
	}

	public Long getChatId() {
		return message.chat().id();
	}

	public Integer getMessageId() {
		return message.messageId();
	}

	/**
	 * Creates a String describing the current request,
	 * writing <b>only</b> the username and if the sender is a new user.
	 *
	 * @return the description of the request
	 */
	public String getDescription() {
		var description = "request from " + getUsername();

		if (START_COMMAND.equals(message.text())) {
			description += NEW_USER;
		}

		return description;
	}

	private String getUsername() {
		return Optional.ofNullable(message.from().username()).orElse("<anonymous>");
	}

	public Answer getAnswerMessage() {
		return isHelpCommand() ? HELP : ABOUT;
	}

	private boolean isHelpCommand() {
		return HELP_COMMAND.equalsIgnoreCase(message.text());
	}

	@Override
	public String toString() {
		String text = Optional.ofNullable(message.text()).orElse(message.caption());

		return "request ["
				+ "chat=" + getChatId()
				+ ", from=" + getUsername()
				+ writeIfNotEmpty("file", getSafeFileId())
				+ writeIfNotEmpty("text", text)
				+ "]";
	}

	private static String writeIfNotEmpty(String field, String value) {
		return value != null && !value.isEmpty()
				? ", " + field + "=" + value
				: "";
	}

	private String getSafeFileId() {
		try {
			return getFileId();
		} catch (TelegramApiException e) {
			return null;
		}
	}
}
