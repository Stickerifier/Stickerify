package com.cellar.stickerify.image;

import org.apache.tika.Tika;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public final class ImageHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageHelper.class);

	/**
	 * @see <a href="https://core.telegram.org/stickers#static-stickers-and-emoji">Telegram documentation</a>
	 */
	private static final int MAX_ALLOWED_SIZE = 512;
	private static final String MIME_TYPE_IMAGE = "image/";
	private static final String PNG_EXTENSION = "png";

	private static final List<String> UNSUPPORTED_FORMATS = List.of("webp", "gif", "gifv");

	/**
	 * Given an image file, it converts it to a png file of the proper dimension (max 512 x 512).
	 *
	 * @param file the file to convert to png
	 * @return a resized and converted png image
	 * @throws TelegramApiException if passed-in file doesn't represent a valid image
	 */
	public static File convertToPng(File file) throws TelegramApiException {
		if (!isValidImage(file)) throw new TelegramApiException("Passed-in file is not supported: " + file.getName());

		try {
			return createPngFile(resizeImage(ImageIO.read(file)));
		} catch (IOException e) {
			LOGGER.error("An unexpected error occurred trying to create resulting image from {} file", file.getName(), e);
			return file;
		}
	}

	/**
	 * Checks if passed-in file represents a valid image.
	 *
	 * @param file the file sent to the bot
	 * @return {@code true} if {@code file} is an image
	 */
	private static boolean isValidImage(File file) {
		boolean isValid = false;

		try {
			String mimeType = new Tika().detect(file);
			isValid = mimeType.startsWith(MIME_TYPE_IMAGE) && isSupportedImage(mimeType);

			LOGGER.info("The file has {} MIME type", mimeType);
		} catch (IOException e) {
			LOGGER.error("Unable to retrieve MIME type for file {}", file.getName());
		}

		return isValid;
	}

	/**
	 * Checks if the MIME type corresponds to a supported one.
	 *
	 * @param mimeType the MIME type to check
	 * @return {@code true} if the MIME type is supported
	 */
	private static boolean isSupportedImage(String mimeType) {
		return UNSUPPORTED_FORMATS.stream().noneMatch(mimeType::endsWith);
	}

	/**
	 * Given an image, it returns its resized version with sides of max 512 pixels each.
	 *
	 * @param originalImage the image to be resized
	 * @return resized image
	 */
	private static BufferedImage resizeImage(BufferedImage originalImage) {
		if (originalImage.getWidth() >= originalImage.getHeight()) {
			return Scalr.resize(originalImage, Mode.FIT_TO_WIDTH, MAX_ALLOWED_SIZE);
		} else {
			return Scalr.resize(originalImage, Mode.FIT_TO_HEIGHT, MAX_ALLOWED_SIZE);
		}
	}

	/**
	 * Creates a new <i>.png</i> file from passed-in {@code image}.
	 *
	 * @param image the image to convert to png
	 * @return png image
	 * @throws IOException if an error occurs creating the png
	 */
	private static File createPngFile(BufferedImage image) throws IOException {
		var pngImage = new File(UUID.randomUUID() + "." + PNG_EXTENSION);
		ImageIO.write(image, PNG_EXTENSION, pngImage);

		return pngImage;
	}

	private ImageHelper() {
		throw new UnsupportedOperationException();
	}
}
