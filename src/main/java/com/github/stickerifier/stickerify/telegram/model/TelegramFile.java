package com.github.stickerifier.stickerify.telegram.model;

import org.jspecify.annotations.Nullable;

public record TelegramFile(String id, @Nullable Long size) {
	private static final String INVALID_ID = "";
	private static final long MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES = 20_000_000L;

	public static final TelegramFile NOT_SUPPORTED = new TelegramFile(INVALID_ID, null);
	public static final TelegramFile TOO_LARGE = new TelegramFile(INVALID_ID, MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES + 1);

	public boolean canBeDownloaded() {
		return size != null && size > 0 && size <= MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES;
	}

	public long sizeValue() {
		return size == null ? -1L : size;
	}
}
