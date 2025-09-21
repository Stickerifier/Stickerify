package com.github.stickerifier.stickerify.runner;

import com.github.stickerifier.stickerify.bot.Stickerify;

public class Main {
	static final Object LOCK = new Object();

	public static void main(String[] args) {
		try (var _ = new Stickerify()) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				synchronized (LOCK) {
					LOCK.notifyAll();
				}
			}));

			synchronized (LOCK) {
				LOCK.wait();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
