package com.github.stickerifier.stickerify.runner;

import com.github.stickerifier.stickerify.bot.Stickerify;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		try (var bot = new Stickerify()) {
			bot.start();
			Thread.currentThread().join();
		}
	}
}
