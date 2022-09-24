package com.cellar.stickerify.bot.model;

public enum TextMessage {

	FILE_READY("Your sticker file is ready!"),
	ABOUT("""
			This bot is open source, you can check it out on [Github](https://github.com/rob93c/Stickerify).

			Looking for sticker packs? Try [MeminiCustom](https://t.me/addstickers/MeminiCustom) and [VideoMemini](https://t.me/addstickers/VideoMemini)!
			"""),
	ERROR("""
			The file conversion was unsuccessful, only _valid image formats_ are supported (`.gif`s and `.webp`s are not supported too).

			If you think it should have worked, please report the issue on [Github](https://github.com/rob93c/Stickerify/issues/new/choose).
			""", true);

	private final String text;
	private final boolean disableWebPreview;

	TextMessage(String text) {
		this.text = text;
		this.disableWebPreview = false;
	}

	TextMessage(String text, boolean disableWebPreview) {
		this.text = text;
		this.disableWebPreview = disableWebPreview;
	}

	public String getText() {
		return text;
	}

	public boolean isDisableWebPreview() {
		return disableWebPreview;
	}
}
