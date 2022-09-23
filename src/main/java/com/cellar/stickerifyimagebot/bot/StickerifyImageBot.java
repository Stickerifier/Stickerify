package com.cellar.stickerifyimagebot.bot;

import com.cellar.stickerifyimagebot.image.ImageHelper;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.logging.Logger;

public class StickerifyImageBot extends TelegramLongPollingBot {

	private static final Logger LOGGER = Logger.getLogger(StickerifyImageBot.class.getSimpleName());

	private static final String FILE_READY_CAPTION = "Your sticker file is ready!";
	private static final String ABOUT_TEXT = """
			This bot is open source, you can check it out on [Github](https://github.com/rob93c/StickerifyImageBot)
			
			Looking for sticker packs? Try [MeminiCustom](https://t.me/addstickers/MeminiCustom) and [VideoMemini](https://t.me/addstickers/VideoMemini)
			""";

	@Override
	public String getBotUsername() {
		return "StickerifyImageBot";
	}

	@Override
	public String getBotToken() {
		return System.getenv("STICKERIFY_TOKEN");
	}

	@Override
	public void onUpdateReceived(Update update) {
		TelegramRequest request = new TelegramRequest(update.getMessage());

		answer(request);
	}

	private void answer(TelegramRequest request) {
		if (request.getFileId() == null) answerDefaultText(request.getChatId());

		answerWithDocument(request);
	}

	private void answerDefaultText(Long chatId) {
		SendMessage response = new SendMessage();
		response.setChatId(chatId);
		response.setText(ABOUT_TEXT);
		response.setParseMode(ParseMode.MARKDOWNV2);

		try {
			execute(response);
		} catch (TelegramApiException e) {
			LOGGER.severe("Unable to send the message: " + e);
		}
	}

	private void answerWithDocument(TelegramRequest request) {
		SendDocument response = new SendDocument();
		response.setChatId(request.getChatId());
		response.setCaption(FILE_READY_CAPTION);

		GetFile getFile = new GetFile(request.getFileId());

		try {
			String filePath = execute(getFile).getFilePath();
			File pngFile = ImageHelper.convertToPng(downloadFile(filePath));
			response.setDocument(new InputFile(pngFile));

			execute(response);
		} catch (TelegramApiException e) {
			LOGGER.severe("Unable to send the message: " + e);
		}
	}
}
