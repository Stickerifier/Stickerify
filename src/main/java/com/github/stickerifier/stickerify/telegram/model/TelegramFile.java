package com.github.stickerifier.stickerify.telegram.model;

public record TelegramFile(String fileId, Long fileSize) {
	private static final long MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES = 20_000_000;

	public boolean canBeDownloaded() {
		return fileSize == null || fileSize < MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES;
	}
}
