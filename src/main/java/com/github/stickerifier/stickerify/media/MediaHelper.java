package com.github.stickerifier.stickerify.media;

import static com.github.stickerifier.stickerify.media.MediaConstraints.MATROSKA_FORMAT;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_DURATION_MILLIS;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_FRAMES;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_SIZE;
import static com.github.stickerifier.stickerify.media.MediaConstraints.VP9_CODEC;

import org.apache.tika.Tika;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class MediaHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MediaHelper.class);

	private static final List<String> SUPPORTED_IMAGES = List.of("image/jpeg", "image/png", "image/webp");
	private static final List<String> SUPPORTED_VIDEOS = List.of("video/quicktime", "video/webm", "application/x-matroska");

	/**
	 * Based on the type of passed-in file, it converts it into the proper media.
	 * If no conversion was needed, {@code null} is returned.
	 *
	 * @param inputFile the file to convert
	 * @return a resized and converted file
	 * @throws TelegramApiException if the file is not supported or if the conversion failed
	 */
	public static File convert(File inputFile) throws TelegramApiException {
		String mimeType = detectMimeType(inputFile);

		if (isSupportedMedia(mimeType, SUPPORTED_IMAGES)) {
			return convertToPng(inputFile, mimeType);
		} else if (isSupportedMedia(mimeType, SUPPORTED_VIDEOS)) {
			return convertToWebm(inputFile);
		} else {
			throw new TelegramApiException("Passed-in file is not supported");
		}
	}

	/**
	 * Analyses the file in order to detect its media type.
	 *
	 * @param file the file sent to the bot
	 * @return the MIME type of the passed-in file
	 */
	private static String detectMimeType(File file) {
		String mimeType = null;

		try {
			mimeType = new Tika().detect(file);

			LOGGER.debug("The file has {} MIME type", mimeType);
		} catch (IOException e) {
			LOGGER.error("Unable to retrieve MIME type for file {}", file.getName());
		}

		return mimeType;
	}

	/**
	 * Checks if the MIME type corresponds to a supported one.
	 *
	 * @param mimeType the MIME type to check
	 * @param supportedFormats the list of the formats to check
	 * @return {@code true} if the MIME type is supported
	 */
	private static boolean isSupportedMedia(String mimeType, List<String> supportedFormats) {
		return supportedFormats.stream().anyMatch(format -> format.equals(mimeType));
	}

	/**
	 * Given an image file, it converts it to a png file of the proper dimension (max 512 x 512).
	 *
	 * @param file the file to convert to png
	 * @param mimeType the MIME type of the file
	 * @return converted image, {@code null} if no conversion was required
	 * @throws TelegramApiException if an error occurred processing passed-in image
	 */
	private static File convertToPng(File file, String mimeType) throws TelegramApiException {
		if (isImageCompliant(file, mimeType)) {
			LOGGER.info("The image doesn't need conversion");

			return null;
		}

		try {
			return createPngFile(resizeImage(ImageIO.read(file)));
		} catch (IOException e) {
			throw new TelegramApiException("An unexpected error occurred trying to create resulting image", e);
		}
	}

	/**
	 * Checks if passed-in file is already compliant with Telegram's requisites.
	 * If so, conversion won't take place and no file will be returned to the user.
	 *
	 * @param file the file to check
	 * @param mimeType the MIME type of the file
	 * @return {@code true} if the file is compliant
	 */
	private static boolean isImageCompliant(File file, String mimeType) throws TelegramApiException {
		try {
			var image = ImageIO.read(file);
			return ("image/png".equals(mimeType) || "image/webp".equals(mimeType))
					&& isSizeCompliant(image.getWidth(), image.getHeight());
		} catch (IOException e) {
			throw new TelegramApiException("An unexpected error occurred processing input image", e);
		}
	}

	/**
	 * Checks that either width or height is 512 pixels
	 * and the other is 512 pixels or fewer.
	 *
	 * @param width the width to be checked
	 * @param height the width to be checked
	 * @return {@code true} if the video has valid dimensions
	 */
	private static boolean isSizeCompliant(int width, int height) {
		return (width == MAX_SIZE && height <= MAX_SIZE) || (height == MAX_SIZE && width <= MAX_SIZE);
	}

	/**
	 * Given an image, it returns its resized version with sides of max 512 pixels each.
	 *
	 * @param image the image to be resized
	 * @return resized image
	 */
	private static BufferedImage resizeImage(BufferedImage image) {
		if (image.getWidth() >= image.getHeight()) {
			return Scalr.resize(image, Mode.FIT_TO_WIDTH, MAX_SIZE);
		} else {
			return Scalr.resize(image, Mode.FIT_TO_HEIGHT, MAX_SIZE);
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
		var pngImage = File.createTempFile("Stickerify-", ".png");
		ImageIO.write(image, "png", pngImage);

		return pngImage;
	}

	/**
	 * Given a video file, it converts it to a webm file of the proper dimension (max 512 x 512),
	 * based on the requirements specified by <a href="https://core.telegram.org/stickers/webm-vp9-encoding">Telegram documentation</a>.
	 *
	 * @param file the file to convert
	 * @return converted video, {@code null} if no conversion was required
	 * @throws TelegramApiException if file conversion is not successful
	 */
	private static File convertToWebm(File file) throws TelegramApiException {
		var mediaInfo = retrieveMultimediaInfo(file);

		if (isVideoCompliant(mediaInfo)) {
			LOGGER.info("The video doesn't need conversion");

			return null;
		}

		return convertWithFFmpeg(file, mediaInfo);
	}

	/**
	 * Convenience method to retrieve multimedia information of a file.
	 *
	 * @param file the video to check
	 * @return passed-in video's multimedia information
	 * @throws TelegramApiException if an error occurred encoding the video
	 */
	private static MultimediaInfo retrieveMultimediaInfo(File file) throws TelegramApiException {
		try {
			return new MultimediaObject(file).getInfo();
		} catch (EncoderException e) {
			throw new TelegramApiException(e);
		}
	}

	/**
	 * Checks if passed-in file is already compliant with Telegram's requisites.
	 * If so, conversion won't take place and no file will be returned to the user.
	 *
	 * @param mediaInfo video's multimedia information
	 * @return {@code true} if the file is compliant
	 */
	private static boolean isVideoCompliant(MultimediaInfo mediaInfo) {
		var videoInfo = mediaInfo.getVideo();
		var videoSize = videoInfo.getSize();

		return isSizeCompliant(videoSize.getWidth(), videoSize.getHeight())
				&& videoInfo.getFrameRate() <= MAX_FRAMES
				&& videoInfo.getDecoder().startsWith(VP9_CODEC)
				&& mediaInfo.getDuration() <= MAX_DURATION_MILLIS
				&& mediaInfo.getAudio() == null
				&& MATROSKA_FORMAT.equals(mediaInfo.getFormat());
	}

	/**
	 * Converts the passed-in file using FFmpeg applying Telegram's animated stickers' constraints.
	 *
	 * @param file the file to convert
	 * @param mediaInfo video's multimedia information
	 * @return converted video
	 * @throws TelegramApiException if file conversion is not successful
	 */
	private static File convertWithFFmpeg(File file, MultimediaInfo mediaInfo) throws TelegramApiException {
		var frameRate = Math.min(mediaInfo.getVideo().getFrameRate(), MAX_FRAMES);
		var duration = Math.min(mediaInfo.getDuration(), MAX_DURATION_MILLIS) / 1_000L;

		try {
			var webmVideo = File.createTempFile("Stickerify-", ".webm");

			var ffmpegCommand = new String[] {
					"ffmpeg",
					"-i", file.getAbsolutePath(),
					"-vf", "scale = 'if(gt(iw,ih)," + MAX_SIZE + ",-2)':'if(gt(iw,ih),-2," + MAX_SIZE + ")', fps = " + frameRate,
					"-c:v", "libvpx-" + VP9_CODEC,
					"-b:v", "256k",
					"-crf", "32",
					"-g", "60",
					"-an",
					"-t", String.valueOf(duration),
					"-y", webmVideo.getAbsolutePath()
			};

			new ProcessBuilder(ffmpegCommand).start().waitFor();

			return webmVideo;
		} catch (IOException | InterruptedException e) {
			throw new TelegramApiException("An error occurred trying to convert passed-in video", e);
		}
	}

	private MediaHelper() {
		throw new UnsupportedOperationException();
	}
}
