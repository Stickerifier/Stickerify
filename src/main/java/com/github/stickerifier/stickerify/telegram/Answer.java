package com.github.stickerifier.stickerify.telegram;

/**
 * Enum class containing the text replies the bot can use.
 */
public enum Answer {

	HELP("""
			Send me the media you want to convert and I will take care of the rest\\.

			Based on what you send, I will answer the following:
			\\- the converted media, if you sent a supported file \\(images, gifs, standard and video stickers are supported\\)
			\\- no file, if you sent a media already suiting Telegram's requirements
			\\- an error message, if you sent either an unsupported or a corrupted file
			\\- an informative message for any message without a file
			"""),
	FILE_READY("""
			Your sticker file is ready\\!
			Head to [Stickers](https://t.me/Stickers) to create a new sticker\\.
			"""),
	FILE_ALREADY_VALID("""
			The media you sent was already suitable to be a Telegram sticker\\.
			Send it to [Stickers](https://t.me/Stickers) to add it as a new sticker\\.
			"""),
	FILE_TOO_LARGE("""
			The file can't be converted because Telegram bots can't handle files larger than 20 MB at the moment: please send a smaller one\\.
			"""),
	ABOUT("""
			This bot is open source, check it out on [Github](https://github.com/Stickerifier/Stickerify)\\.

			Looking for sticker packs? Try [MeminiCustom](https://t.me/addstickers/MeminiCustom) and [VideoMemini](https://t.me/addstickers/VideoMemini)\\!
			"""),
	ERROR("""
			The file conversion was unsuccessful: only images, gifs, standard and video stickers are supported\\.

			If you think it should have worked, please report the issue on [Github](https://github.com/Stickerifier/Stickerify/issues/new/choose)\\.
			""", true),
	CORRUPTED("""
			The conversion was unsuccessful: the video might be corrupted and it cannot be processed\\.

			If you think it should have worked, please report the issue on [Github](https://github.com/Stickerifier/Stickerify/issues/new/choose)\\.
			""", true),
	PRIVACY_POLICY("""
			You can view our privacy policy by visiting [this link](https://github.com/Stickerifier/Stickerify/blob/main/PRIVACY_POLICY.md)\\.

			If you have any questions or concerns, feel free to reach out to us\\.
			""", true);

	private final String text;
	private final boolean disableWebPreview;

	Answer(String text) {
		this(text, false);
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
