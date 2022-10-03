package com.cellar.stickerify.telegram;

/**
 * Enum class containing the text answers the bot can use.
 */
public enum Answer {

	HELP("""
			Send me the image you want to convert and I will take care of the rest\\.

			Based on what you send, I will answer the following:
			\\- the converted image, if you sent a supported file \\(currently `\\.gif`, `\\.gifv` and `\\.webp` files are not supported\\)
			\\- an error message, if you sent an unsupported file
			\\- an informative message for any message without a file
			"""),
	FILE_READY("""
            Your sticker file is ready\\!
            Head to [Stickers](https://t.me/Stickers) to create a new sticker\\.
            """),
	ABOUT("""
			This bot is open source, check it out on [Github](https://github.com/rob93c/Stickerify)\\.

			Looking for sticker packs? Try [MeminiCustom](https://t.me/addstickers/MeminiCustom) and [VideoMemini](https://t.me/addstickers/VideoMeminis)\\!
			"""),
	ERROR("""
			The file conversion was unsuccessful, only _valid image formats_ are supported \\(currently `\\.gif`, `\\.gifv` and `\\.webp` files are not supported\\)\\.

			If you think it should have worked, please report the issue on [Github](https://github.com/rob93c/Stickerify/issues/new/choose)\\.
			""", true);

	private final String text;
	private final boolean disableWebPreview;

	Answer(String text) {
		this.text = text;
		this.disableWebPreview = false;
	}

	Answer(String text, boolean disableWebPreview) {
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
