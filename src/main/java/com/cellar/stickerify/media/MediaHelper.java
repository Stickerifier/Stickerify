package com.cellar.stickerify.media;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;

public final class MediaHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MediaHelper.class);

	public static File convert(File originalFile) throws TelegramApiException {
		if (isImage(originalFile)) {
			return ImageHelper.convertToPng(originalFile);
		} else {
			return VideoHelper.convertToWebm(originalFile);
		}
	}

	private static boolean isImage(File file) {
		try {
			return new Tika().detect(file).startsWith("image/");
		} catch (IOException e) {
			LOGGER.error("Unable to retrieve MIME type for file {}", file.getName());
			return false;
		}
	}

	private MediaHelper() {
		throw new UnsupportedOperationException();
	}
}
