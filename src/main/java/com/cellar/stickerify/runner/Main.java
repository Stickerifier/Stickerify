package com.cellar.stickerify.runner;

import com.cellar.stickerify.bot.Stickerify;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

	public static void main(String[] args) {
		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(new Stickerify());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
