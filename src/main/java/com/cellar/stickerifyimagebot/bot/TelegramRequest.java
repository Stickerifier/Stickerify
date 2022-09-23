package com.cellar.stickerifyimagebot.bot;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import static java.util.Comparator.comparing;

public record TelegramRequest(Message message) {

	String getFileId() {
		String fileId = null;

		if (message.hasPhoto()) {
			fileId = message.getPhoto().stream().max(comparing(PhotoSize::getFileSize)).orElseThrow().getFileId();
		} else if (message.hasDocument()) {
			fileId = message.getDocument().getFileId();
		}

		return fileId;
	}

	Long getChatId() {
		return message.getChatId();
	}

}
