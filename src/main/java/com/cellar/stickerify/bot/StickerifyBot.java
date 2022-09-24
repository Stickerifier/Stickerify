package com.cellar.stickerify.bot;

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
import java.util.logging.Logger;

public class StickerifyBot extends TelegramLongPollingBot {

	private static final Logger LOGGER = Logger.getLogger(StickerifyBot.class.getSimpleName());

	private static final String FILE_READY_CAPTION = "Your sticker file is ready!";
	private static final String ABOUT_TEXT = """
			This bot is open source, you can check it out on [Github](https://github.com/rob93c/StickerifyImageBot)\\.
			
			Looking for sticker packs? Try [MeminiCustom](https://t.me/addstickers/MeminiCustom) and [VideoMemini](https://t.me/addstickers/VideoMemini)\\!
			""";
	private static final String ERROR_TEXT = """
            The file conversion was unsuccessful, only __valid image formats__ are supported \\(also `gif` and `webp` are not supported\\)\\.
            If you think it should have worked, please report the issue on [Github](https://github.com/rob93c/StickerifyImageBot/issues/new/choose)\\.
            """;

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
		TelegramRequest request = new TelegramRequest(update.getMessage());

		answer(request);
	}

	private void answer(TelegramRequest request) {
		if (request.getFileId() == null) {
			answerText(ABOUT_TEXT, request.getChatId());
		} else {
			answerWithDocument(request);
		}
	}

	private void answerText(String text, Long chatId) {
		SendMessage response = new SendMessage();
		response.setChatId(chatId);
		response.setText(text);
		response.setParseMode(ParseMode.MARKDOWNV2);

		try {
			execute(response);
		} catch (TelegramApiException e) {
			LOGGER.severe("Unable to send the message: " + e);
			answerText(ERROR_TEXT, chatId);
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
			answerText(ERROR_TEXT, request.getChatId());
		}
	}
}
