package com.cellar.stickerify.telegram.model;

import com.cellar.stickerify.telegram.Answer;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import static java.util.Comparator.comparing;

public record TelegramRequest(Message message) {

	private static final String HELP_COMMAND = "/help";

	public String getFileId() {
		String fileId = null;

		if (message.hasPhoto()) {
			fileId = message.getPhoto().stream().max(comparing(PhotoSize::getFileSize)).orElseThrow().getFileId();
		} else if (message.hasDocument()) {
			fileId = message.getDocument().getFileId();
		} else if (message.hasSticker()) {
			fileId = message.getSticker().getFileId();
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
		return isHelpCommand() ? Answer.HELP : Answer.ABOUT;
	}

	private boolean isHelpCommand() {
		return HELP_COMMAND.equalsIgnoreCase(message.getText());
	}
}
