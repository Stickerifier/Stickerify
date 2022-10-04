package com.cellar.stickerify.bot;

import com.cellar.stickerify.image.ImageHelper;
import com.cellar.stickerify.telegram.Answer;
import com.cellar.stickerify.telegram.builder.DocumentMessageBuilder;
import com.cellar.stickerify.telegram.builder.TextMessageBuilder;
import com.cellar.stickerify.telegram.model.TelegramRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A Telegram bot to convert images in the format required to be used as Telegram stickers (512x512 PNGs).
 *
 * @author Roberto Cella
 */
public class StickerifyBot extends TelegramLongPollingBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(StickerifyBot.class);

	@Override
	public String getBotUsername() {
		return "Stickerify";
	}

	@Override
	public String getBotToken() { return System.getenv("STICKERIFY_TOKEN"); }

	@Override
	public void onUpdateReceived(Update update) {
		if (update.getMessage() != null) {
			TelegramRequest request = new TelegramRequest(update.getMessage());

			answer(request);
		} else {
			LOGGER.info("Updated messages don't need to be handled");
		}
	}

	private void answer(TelegramRequest request) {
		if (request.getFileId() == null) {
			answerText(request.getAnswerMessage(), request.getChatId());
		} else {
			answerWithDocument(request);
		}
	}

	private void answerText(Answer answer, Long chatId) {
		SendMessage response = new TextMessageBuilder()
				.withChatId(chatId)
				.withAnswer(answer)
				.build();

		try {
			execute(response);
		} catch (TelegramApiException e) {
			LOGGER.error("Unable to send the message", e);
		}
	}

	private void answerWithDocument(TelegramRequest request) {
		File pngFile = null;

		GetFile getFile = new GetFile(request.getFileId());

		try {
			String filePath = execute(getFile).getFilePath();
			pngFile = ImageHelper.convertToPng(downloadFile(filePath));

			SendDocument response = new DocumentMessageBuilder()
					.withChatId(request.getChatId())
					.withAnswer(Answer.FILE_READY)
					.withReplyToMessageId(request.getMessageId())
					.withDocument(pngFile)
					.build();

			execute(response);
		} catch (TelegramApiException e) {
			LOGGER.error("Unable to send the message", e);
			answerText(Answer.ERROR, request.getChatId());
		} finally {
			if (pngFile != null) deleteTempFile(pngFile);
		}
	}

	private static void deleteTempFile(File file) {
		try {
			Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			LOGGER.error("An error occurred trying to delete generated image", e);
		}
	}
}
