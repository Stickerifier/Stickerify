package com.cellar.stickerify.image;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Logger;

public class ImageHelper {

	private static final Logger LOGGER = Logger.getLogger(ImageHelper.class.getSimpleName());

	private static final String MIME_TYPE_IMAGE = "image/";
	private static final String PNG_EXTENSION = "png";
	/**
	 * @see <a href="https://core.telegram.org/stickers#static-stickers-and-emoji">Telegram documentation</a>
	 */
	private static final int MAX_ALLOWED_SIZE = 512;

	/**
	 * Given an image file, it converts it to a png file of the proper dimension (max 512 x 512).
	 *
	 * @param imageFile the file to convert to png
	 * @return a resized and converted png image
	 * @throws TelegramApiException if passed-in file doesn't represent an image
	 */
	public static File convertToPng(File imageFile) throws TelegramApiException {
		if (!isSupportedFormat(imageFile)) throw new TelegramApiException("Passed-in file is not a valid image");

		try {
			return createPngFile(resizeImage(ImageIO.read(imageFile)));
		} catch (IOException e) {
			return imageFile;
		}
	}

	/**
	 * Checks if passed-in file represents a valid image.
	 *
	 * @param file the file sent to the bot
	 * @return true if {@code file} is an image
	 */
	private static boolean isSupportedFormat(File file) {
		boolean isSupported = false;

		try {
			String mimeType = Files.probeContentType(file.toPath());
			isSupported = mimeType.startsWith(MIME_TYPE_IMAGE);

			LOGGER.fine("The file has MIME type " + mimeType);
		} catch (IOException e) {
			LOGGER.severe("Unable to retrieve mime type for file " + file.getName());
		}

		return isSupported;
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

	private ImageHelper() {}
}
