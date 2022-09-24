package com.cellar.stickerify.bot.model;

public enum TextMessage {

	FILE_READY("Your sticker file is ready!"),
	ABOUT("""
			This bot is open source, you can check it out on [Github](https://github.com/rob93c/Stickerify)\\.

			Looking for sticker packs? Try [MeminiCustom](https://t.me/addstickers/MeminiCustom) and [VideoMemini](https://t.me/addstickers/VideoMemini)\\!
			"""),
	ERROR("""
			The file conversion was unsuccessful, only __valid image formats__ are supported \\(also `gif` and `webp` are not supported\\)\\.

			If you think it should have worked, please report the issue on [Github](https://github.com/rob93c/Stickerify/issues/new/choose)\\.
			""", true);

	private final String text;
	private final boolean disableWebPreview;

	private TextMessage(String text) {
		this.text = text;
		this.disableWebPreview = false;
	}

	private TextMessage(String text, boolean disableWebPreview) {
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
