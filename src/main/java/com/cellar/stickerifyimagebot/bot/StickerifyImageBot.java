package com.cellar.stickerifyimagebot.bot;

import com.cellar.stickerifyimagebot.image.ImageHelper;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.logging.Logger;

import static java.util.Comparator.comparing;

public class StickerifyImageBot extends TelegramLongPollingBot {

	private static final Logger LOGGER = Logger.getLogger(StickerifyImageBot.class.getSimpleName());

	private static final String CAPTION = "Your sticker file is ready!";

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
		var images = update.getMessage().getPhoto();
		var bestImage = images.stream().max(comparing(PhotoSize::getFileSize)).orElseThrow();

		SendDocument response = new SendDocument();
		response.setChatId(update.getMessage().getChatId());
		response.setCaption(CAPTION);

		GetFile getFile = new GetFile(bestImage.getFileId());

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
