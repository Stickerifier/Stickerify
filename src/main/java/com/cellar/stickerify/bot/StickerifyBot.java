package com.cellar.stickerify.bot;

import com.cellar.stickerify.bot.model.TelegramRequest;
import com.cellar.stickerify.bot.model.TextMessage;
import com.cellar.stickerify.image.ImageHelper;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

public class StickerifyBot extends TelegramLongPollingBot {

	private static final Logger LOGGER = Logger.getLogger(StickerifyBot.class.getSimpleName());

	@Override
	public String getBotUsername() {
		return "Stickerify";
	}

	@Override
	public String getBotToken() {
		return System.getenv("STICKERIFY_TOKEN");
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.getMessage() != null) {
			TelegramRequest request = new TelegramRequest(update.getMessage());

			answer(request);
		}
	}

	private void answer(TelegramRequest request) {
		if (request.getFileId() == null) {
			answerText(TextMessage.ABOUT, request.getChatId());
		} else {
			answerWithDocument(request);
		}
	}

	private void answerText(TextMessage textMessage, Long chatId) {
		SendMessage response = new SendMessage();
		response.setChatId(chatId);
		response.setText(textMessage.getText());
		response.setParseMode(ParseMode.MARKDOWN);
		response.setDisableWebPagePreview(textMessage.isDisableWebPreview());

		try {
			execute(response);
		} catch (TelegramApiException e) {
			LOGGER.severe("Unable to send the message: " + e);
		}
	}

	private void answerWithDocument(TelegramRequest request) {
		SendDocument response = new SendDocument();
		response.setChatId(request.getChatId());
		response.setCaption(TextMessage.FILE_READY.getText());

		GetFile getFile = new GetFile(request.getFileId());

		try {
			String filePath = execute(getFile).getFilePath();
			File pngFile = ImageHelper.convertToPng(downloadFile(filePath));
			response.setDocument(new InputFile(pngFile));

			execute(response);

			Files.deleteIfExists(pngFile.toPath());
		} catch (TelegramApiException | IOException e) {
			LOGGER.severe("Unable to send the message: " + e);
			answerText(TextMessage.ERROR, request.getChatId());
		}
	}
}
