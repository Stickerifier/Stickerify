package com.github.stickerifier.stickerify.telegram.model;

public record TelegramFile(String id, long size) {
	private static final String INVALID_ID = "";
	private static final long INVALID_SIZE = -1L;
	private static final long MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES = 20_000_000L;

	public static final TelegramFile NOT_SUPPORTED = new TelegramFile(INVALID_ID);
	public static final TelegramFile TOO_LARGE = new TelegramFile(INVALID_ID, MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES + 1);

	public TelegramFile(String id) {
		this(id, INVALID_SIZE);
	}

	public boolean canBeDownloaded() {
		return size <= MAX_DOWNLOADABLE_FILE_SIZE_IN_BYTES;
	}
}
