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

	private static final String HELP_COMMAND = "/help";

	public boolean hasFile() {
		return message.hasPhoto() || message.hasDocument() || message.hasSticker() || message.hasVideo() || message.hasAudio();
	}

	public String getFileId() throws TelegramApiException {
		String fileId;

		if (message.hasPhoto()) {
			fileId = message.getPhoto().stream().max(comparing(PhotoSize::getFileSize)).orElseThrow().getFileId();
		} else if (message.hasDocument()) {
			fileId = message.getDocument().getFileId();
		} else if (message.hasSticker()) {
			fileId = message.getSticker().getFileId();
		} else {
			throw new TelegramApiException("The request contains an unsupported media: " + message);
		}

		return fileId;
	}

	public Long getChatId() {
		return message.getChatId();
	}

	public Integer getMessageId() {
		return message.getMessageId();
	}

	public Answer getAnswerMessage() {
		return isHelpCommand() ? HELP : ABOUT;
	}

	private boolean isHelpCommand() {
		return HELP_COMMAND.equalsIgnoreCase(message.getText());
	}

	@Override
	public String toString() {
		String username = Optional.ofNullable(message.getFrom().getUserName()).orElse("<anonymous>");
		String text = Optional.ofNullable(message.getText()).orElse(message.getCaption());

		return "request ["
				+ "chat=" + getChatId()
				+ ", from=" + username
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
