package com.cellar.stickerify.telegram.model;

import static com.cellar.stickerify.telegram.Answer.ABOUT;
import static com.cellar.stickerify.telegram.Answer.HELP;
import static java.util.Comparator.comparing;

import com.cellar.stickerify.telegram.Answer;
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
		return getSafeFileId() != null;
	}

	private String getSafeFileId() {
		try {
			return getFileId();
		} catch (TelegramApiException e) {
			return null;
		}
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
		String text = Optional.ofNullable(message.getText()).orElse(message.getCaption());

		return "request ["
				+ "chat=" + getChatId()
				+ ", from=" + message.getFrom().getUserName()
				+ ", file=" + getSafeFileId()
				+ ", text=" + text
				+ "]";
	}
}
