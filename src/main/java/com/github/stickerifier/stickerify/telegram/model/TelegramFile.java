package com.github.stickerifier.stickerify.telegram.model;

public record TelegramFile(String fileId, Long fileSize) {
	public static final TelegramFile NOT_SUPPORTED = new TelegramFile(null, null);
	public static final TelegramFile TOO_LARGE = new TelegramFile(null, Long.MAX_VALUE);

	private static final long MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES = 20_000_000;

	public boolean canBeDownloaded() {
		return fileSize == null || fileSize <= MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES;
	}
}
