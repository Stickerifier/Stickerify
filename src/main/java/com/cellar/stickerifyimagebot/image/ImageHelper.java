package com.cellar.stickerifyimagebot.image;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ImageHelper {

	private static final String PNG_EXTENSION = "png";
	/**
	 * {@see <a href="https://core.telegram.org/stickers#static-stickers-and-emoji">Telegram documentation</a>}
	 */
	private static final int MAX_ALLOWED_SIZE = 512;

	/**
	 * Given an image file, it converts it to a png file of the proper dimension (max 512 x 512).
	 *
	 * @param imageFile the file to convert to png
	 * @return a resized and converted png image
	 */
	public static File convertToPng(File imageFile) {
		try {
			return createPngFile(resizeImage(ImageIO.read(imageFile)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	 * @throws IOException
	 */
	private static File createPngFile(BufferedImage image) throws IOException {
		File pngImage = new File(UUID.randomUUID() + "." + PNG_EXTENSION);
		ImageIO.write(image, PNG_EXTENSION, pngImage);

		return pngImage;
	}

	private ImageHelper() {}
}
