package com.github.stickerifier.stickerify.telegram.model;

import static com.github.stickerifier.stickerify.telegram.Answer.ABOUT;
import static com.github.stickerifier.stickerify.telegram.Answer.HELP;
import static java.util.Comparator.comparing;

import com.github.stickerifier.stickerify.telegram.Answer;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

/**
 * Data class wrapping a {@link Message} instance to represent a Telegram request.
 *
 * @param message the message to wrap
 */
public record TelegramRequest(Message message) {

	private static final String START_COMMAND = "/start";
	private static final String HELP_COMMAND = "/help";

	public boolean hasFile() {
		return message.hasPhoto() || message.hasDocument() || message.hasSticker()
				|| message.hasVideo() || message.hasVideoNote()
				|| message.hasAudio() || message.hasVoice();
	}

	public String getFileId() throws TelegramApiException {
		if (message.hasPhoto()) {
			return message.getPhoto().stream().max(comparing(PhotoSize::getFileSize)).orElseThrow().getFileId();
		} else if (message.hasDocument()) {
			return message.getDocument().getFileId();
		} else if (message.hasSticker()) {
			return message.getSticker().getFileId();
		} else if (message.hasVideo()) {
			return message.getVideo().getFileId();
		} else if (message.hasVideoNote()) {
			return message.getVideoNote().getFileId();
		}

		throw new TelegramApiException("The request contains an unsupported media: " + message);
	}

	public Long getChatId() {
		return message.getChatId();
	}

	public Integer getMessageId() {
		return message.getMessageId();
	}

	/**
	 * Creates a String describing the current request,
	 * writing <b>only</b> the username and if the sender is a new user.
	 *
	 * @return the description of the request
	 */
	public String getDescription() {
		var description = "request from " + getUsername();

		if (START_COMMAND.equals(message.getText())) {
			description += " (new user)";
		}

		return description;
	}

	private String getUsername() {
		return Optional.ofNullable(message.getFrom().getUserName()).orElse("<anonymous>");
	}

	public Answer getAnswerMessage() {
		return isHelpCommand() ? HELP : ABOUT;
	}

	private boolean isHelpCommand() {
		return HELP_COMMAND.equalsIgnoreCase(message.getText());
	}

	@Override
	public String toString() {
		String text = Optional.ofNullable(message.getText()).orElse(message.getCaption());

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
