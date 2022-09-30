package com.cellar.stickerify.telegram;

/**
 * Enum class containing the text responses the bot can use.
 */
public enum Message {

	FILE_READY("""
            Your sticker file is ready!
            Head to [Stickers](https://t.me/Stickers) to create a new sticker.
            """),
	ABOUT("""
			This bot is open source, check it out on [Github](https://github.com/rob93c/Stickerify).

			Looking for sticker packs? Try [MeminiCustom](https://t.me/addstickers/MeminiCustom) and [VideoMemini](https://t.me/addstickers/VideoMemini)!
			"""),
	ERROR("""
			The file conversion was unsuccessful, only _valid image formats_ are supported (currently `.gif`, `.gifv` and `.webp` files are not supported).

			If you think it should have worked, please report the issue on [Github](https://github.com/rob93c/Stickerify/issues/new/choose).
			""", true);

	private final String text;
	private final boolean disableWebPreview;

	Message(String text) {
		this.text = text;
		this.disableWebPreview = false;
	}

	Message(String text, boolean disableWebPreview) {
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
