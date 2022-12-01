package com.cellar.stickerify.media;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.io.IOException;
import java.util.List;

final class VideoHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(VideoHelper.class);

	/**
	 * @see <a href="https://core.telegram.org/stickers/webm-vp9-encoding">Telegram documentation</a>
	 */
	private static final VideoSize MAX_ALLOWED_SIZE = new VideoSize(512, 512);

	private static final List<String> SUPPORTED_FORMATS = List.of("video/quicktime");

	static File convertToWebm(File file) throws TelegramApiException {
		if (!isValidVideo(file)) throw new TelegramApiException("Passed-in file is not supported");

		try {
			return createWebmFile(file);
		} catch (IOException | EncoderException e) {
			throw new TelegramApiException("An unexpected error occurred trying to create resulting image", e);
		}
	}

	private static boolean isValidVideo(File file) {
		boolean isValid = false;

		try {
			String mimeType = new Tika().detect(file);
			isValid = SUPPORTED_FORMATS.stream().anyMatch(mimeType::equals);

			LOGGER.debug("The file has {} MIME type", mimeType);
		} catch (IOException e) {
			LOGGER.error("Unable to retrieve MIME type for file {}", file.getName());
		}

		return isValid;
	}

	private static File createWebmFile(File file) throws EncoderException, IOException {
		var video = new VideoAttributes()
				.setPixelFormat("yuv420p")
				.setCodec("vp9")
				.setFrameRate(30)
				.setSize(MAX_ALLOWED_SIZE);

		var attributes = new EncodingAttributes()
				.setDuration(3.0F)
				.setAudioAttributes(null)
				.setLoop(true)
				.setVideoAttributes(video);

		var webmFile = File.createTempFile("Stickerify-", ".webm");
		new Encoder().encode(new MultimediaObject(file), webmFile, attributes);

		return webmFile;
	}

	private VideoHelper() {
		throw new UnsupportedOperationException();
	}
}
