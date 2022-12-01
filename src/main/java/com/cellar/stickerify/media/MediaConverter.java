package com.cellar.stickerify.media;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;

public final class MediaConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MediaConverter.class);

	public static File convert(File file) throws TelegramApiException {
		if (isImage(file)) {
			return ImageHelper.convertToPng(file);
		} else {
			return VideoHelper.convertToWebm(file);
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

	private MediaConverter() {
		throw new UnsupportedOperationException();
	}
}
