package com.github.stickerifier.stickerify.runner;

import com.github.stickerifier.stickerify.bot.Stickerify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(new Stickerify());
		} catch (TelegramApiException e) {
			LOGGER.warn("An unexpected error occurred: {}", e.getMessage());
		}
	}
}
