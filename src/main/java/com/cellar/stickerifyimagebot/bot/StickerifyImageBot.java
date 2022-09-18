package com.cellar.stickerifyimagebot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Logger;

public class StickerifyImageBot extends TelegramLongPollingBot {

	private static final Logger LOGGER = Logger.getLogger(StickerifyImageBot.class.getSimpleName());

	private static final String SINGULAR_RESPONSE = "Your sticker file is ready!";
	private static final String PLURAL_RESPONSE = "Your sticker files are ready!";

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
		var photos = update.getMessage().getPhoto();
		//List<File> pngFiles = photos.forEach(ImageHelper::convertToPng);

		String chatId = update.getMessage().getChatId().toString();
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(chatId);
		//sendMessage.setEntities(pngFiles);
		sendMessage.setText(photos.size() > 1 ? PLURAL_RESPONSE : SINGULAR_RESPONSE);

		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			LOGGER.severe("Unable to send the message: " + e);
		}

	}
}
