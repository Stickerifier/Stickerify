package com.github.stickerifier.stickerify.media;

import static com.github.stickerifier.stickerify.media.MediaConstraints.MATROSKA_FORMAT;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_ANIMATION_DURATION_SECONDS;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_ANIMATION_FILE_SIZE;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_ANIMATION_FRAME_RATE;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_IMAGE_FILE_SIZE;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_SIDE_LENGTH;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_VIDEO_DURATION_MILLIS;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_VIDEO_FILE_SIZE;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_VIDEO_FRAMES;
import static com.github.stickerifier.stickerify.media.MediaConstraints.VP9_CODEC;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.stickerifier.stickerify.exception.CorruptedVideoException;
import com.github.stickerifier.stickerify.exception.FileOperationException;
import com.github.stickerifier.stickerify.exception.MediaException;
import com.github.stickerifier.stickerify.exception.ProcessException;
import com.github.stickerifier.stickerify.process.OsConstants;
import com.github.stickerifier.stickerify.process.PathLocator;
import com.github.stickerifier.stickerify.process.ProcessHelper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import org.apache.tika.Tika;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class MediaHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(MediaHelper.class);

	private static final Tika TIKA = new Tika();
	private static final Gson GSON = new Gson();
	private static final List<String> SUPPORTED_VIDEOS = List.of("image/gif", "video/quicktime", "video/webm",
			"video/mp4", "video/x-m4v", "application/x-matroska", "video/x-msvideo");

	private static final int IMAGE_KEEP_ASPECT_RATIO = -1;
	private static final int VIDEO_KEEP_ASPECT_RATIO = -2;

	/**
	 * Based on the type of passed-in file, it converts it into the proper media.
	 * If no conversion was needed, {@code null} is returned.
	 *
	 * @param inputFile the file to convert
	 * @return a resized and converted file
	 * @throws MediaException if the file is not supported or if the conversion failed
	 * @throws InterruptedException if the current thread is interrupted while converting a video file
	 */
	public static @Nullable File convert(File inputFile) throws MediaException, InterruptedException {
		var mimeType = detectMimeType(inputFile);

		try {
			if (isSupportedVideo(mimeType)) {
				if (isVideoCompliant(inputFile)) {
					LOGGER.atInfo().log("The video doesn't need conversion");
					return null;
				}

				return convertToWebm(inputFile);
			}

			if (isAnimatedStickerCompliant(inputFile, mimeType)) {
				LOGGER.atInfo().log("The animated sticker doesn't need conversion");
				return null;
			}

			if (mimeType.startsWith("image/")) {
				if (isImageCompliant(inputFile, mimeType)) {
					LOGGER.atInfo().log("The image doesn't need conversion");
					return null;
				}

				return convertToWebp(inputFile);
			}
		} catch (MediaException e) {
			LOGGER.atWarn().setCause(e).log("The file with {} MIME type could not be converted", mimeType);
			throw e;
		}

		throw new MediaException("The file with {} MIME type is not supported", mimeType);
	}

	/**
	 * Analyzes the file to detect its media type.
	 *
	 * @param file the file sent to the bot
	 * @return the MIME type of the passed-in file
	 * @throws MediaException if the file could not be read
	 */
	private static String detectMimeType(File file) throws MediaException {
		try {
			var mimeType = TIKA.detect(file);
			LOGGER.atDebug().log("The file has {} MIME type", mimeType);

			return mimeType;
		} catch (IOException e) {
			LOGGER.atError().log("Unable to retrieve MIME type for file {}", file.getName());
			throw new MediaException(e);
		}
	}

	/**
	 * Checks if the MIME type corresponds to one of the supported video formats.
	 *
	 * @param mimeType the MIME type to check
	 * @return {@code true} if the MIME type is supported
	 */
	private static boolean isSupportedVideo(String mimeType) {
		return SUPPORTED_VIDEOS.stream().anyMatch(format -> format.equals(mimeType));
	}

	/**
	 * Checks if passed-in file is already compliant with Telegram's requisites.
	 * If so, conversion won't take place and no file will be returned to the user.
	 *
	 * @param file the file to check
	 * @return {@code true} if the file is compliant
	 * @throws FileOperationException if an error occurred retrieving the size of the file
	 */
	private static boolean isVideoCompliant(File file) throws FileOperationException, CorruptedVideoException {
		var mediaInfo = retrieveMultimediaInfo(file);
		var videoInfo = mediaInfo.getVideo();
		var videoSize = videoInfo.getSize();

		return isSizeCompliant(videoSize.getWidth(), videoSize.getHeight())
				&& videoInfo.getFrameRate() <= MAX_VIDEO_FRAMES
				&& videoInfo.getDecoder().startsWith(VP9_CODEC)
				&& mediaInfo.getDuration() > 0L
				&& mediaInfo.getDuration() <= MAX_VIDEO_DURATION_MILLIS
				&& mediaInfo.getAudio() == null
				&& MATROSKA_FORMAT.equals(mediaInfo.getFormat())
				&& isFileSizeLowerThan(file, MAX_VIDEO_FILE_SIZE);
	}

	/**
	 * Convenience method to retrieve multimedia information of a file.
	 *
	 * @param file the video to check
	 * @return passed-in video's multimedia information
	 * @throws CorruptedVideoException if an error occurred retrieving video information
	 */
	private static MultimediaInfo retrieveMultimediaInfo(File file) throws CorruptedVideoException {
		try {
			return new MultimediaObject(file, PathLocator.INSTANCE).getInfo();
		} catch (EncoderException e) {
			throw new CorruptedVideoException("The video could not be processed successfully", e);
		}
	}

	/**
	 * Checks if the file is a {@code gzip} archive, then it reads its content and verifies if it's a valid JSON.
	 * Once JSON information is retrieved, they are validated against Telegram's requirements.
	 *
	 * @param file the file to check
	 * @param mimeType the MIME type of the file
	 * @return {@code true} if the file is compliant
	 * @throws FileOperationException if an error occurred retrieving the size of the file
	 */
	private static boolean isAnimatedStickerCompliant(File file, String mimeType) throws FileOperationException {
		if ("application/gzip".equals(mimeType)) {
			var uncompressedContent = "";

			try (var gzipInputStream = new GZIPInputStream(new FileInputStream(file))) {
				uncompressedContent = new String(gzipInputStream.readAllBytes(), UTF_8);
			} catch (IOException _) {
				LOGGER.atError().log("Unable to retrieve gzip content from file {}", file.getName());
			}

			try {
				var sticker = GSON.fromJson(uncompressedContent, AnimationDetails.class);

				boolean isAnimationCompliant = isAnimationCompliant(sticker);
				if (isAnimationCompliant) {
					return isFileSizeLowerThan(file, MAX_ANIMATION_FILE_SIZE);
				}

				LOGGER.atWarn().log("The {} doesn't meet Telegram's requirements", sticker);
			} catch (JsonSyntaxException _) {
				LOGGER.atInfo().log("The archive isn't an animated sticker");
			}
		}

		return false;
	}

	private record AnimationDetails(@SerializedName("w") int width, @SerializedName("h") int height, @SerializedName("fr") int frameRate, @SerializedName("ip") float start, @SerializedName("op") float end) {
		private float duration() {
			return (end - start) / frameRate;
		}

		@NonNull
		@Override
		public String toString() {
			return "animated sticker [" +
					"width=" + width +
					", height=" + height +
					", frameRate=" + frameRate +
					", duration=" + duration() +
					']';
		}
	}

	/**
	 * Checks if passed-in animation is already compliant with Telegram's requisites.
	 * If so, conversion won't take place and no file will be returned to the user.
	 *
	 * @param animation the animation to check
	 * @return {@code true} if the animation is compliant
	 */
	private static boolean isAnimationCompliant(AnimationDetails animation) {
		return animation != null
				&& animation.frameRate() <= MAX_ANIMATION_FRAME_RATE
				&& animation.duration() <= MAX_ANIMATION_DURATION_SECONDS
				&& animation.width() == MAX_SIDE_LENGTH
				&& animation.height() == MAX_SIDE_LENGTH;
	}

	/**
	 * Checks that passed-in file's size does not exceed the specified threshold.
	 *
	 * @param file the file to check
	 * @param threshold max allowed file size
	 * @return {@code true} if file's size is compliant
	 * @throws FileOperationException if an error occurred retrieving the size of the file
	 */
	private static boolean isFileSizeLowerThan(File file, long threshold) throws FileOperationException {
		try {
			return Files.size(file.toPath()) <= threshold;
		} catch (IOException e) {
			throw new FileOperationException(e);
		}
	}

	/**
	 * Checks if passed-in image is already compliant with Telegram's requisites.
	 * If so, conversion won't take place and no file will be returned to the user.
	 *
	 * @param image the image to check
	 * @param mimeType the MIME type of the file
	 * @return {@code true} if the file is compliant
	 */
	private static boolean isImageCompliant(File image, String mimeType) throws CorruptedVideoException, FileOperationException {
		var videoSize = retrieveMultimediaInfo(image).getVideo().getSize();
		if (videoSize == null) {
			throw new FileOperationException("Couldn't read image dimensions");
		}

		return ("image/png".equals(mimeType) || "image/webp".equals(mimeType))
				&& isSizeCompliant(videoSize.getWidth(), videoSize.getHeight())
				&& isFileSizeLowerThan(image, MAX_IMAGE_FILE_SIZE);
	}

	/**
	 * Given an image file, it converts it to a WebP file of the proper dimension (max 512 x 512).
	 *
	 * @param file the image to convert to WebP
	 * @return converted image
	 * @throws MediaException if an error occurred processing passed-in image
	 * @throws InterruptedException if the current thread is interrupted while converting the file
	 */
	private static File convertToWebp(File file) throws MediaException, InterruptedException {
		var webpImage = createTempFile("webp");
		var command = new String[] {
				"ffmpeg",
				"-y",
				"-v", "error",
				"-i", file.getAbsolutePath(),
				"-vf", "scale='if(gt(iw,ih),%1$d,%2$d)':'if(gt(iw,ih),%2$d,%1$d)'".formatted(MAX_SIDE_LENGTH, IMAGE_KEEP_ASPECT_RATIO),
				"-c:v", "libwebp",
				"-lossless", "1",
				"-q:v", "100",
				webpImage.getAbsolutePath()
		};

		try {
			ProcessHelper.executeCommand(command);
		} catch (ProcessException e) {
			try {
				deleteFile(webpImage);
			} catch (FileOperationException ex) {
				e.addSuppressed(ex);
			}
			throw new MediaException("FFmpeg image conversion failed", e);
		}

		return webpImage;
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
		return (width == MAX_SIDE_LENGTH && height <= MAX_SIDE_LENGTH) || (height == MAX_SIDE_LENGTH && width <= MAX_SIDE_LENGTH);
	}

	/**
	 * Creates a new temp file with the desired extension.
	 *
	 * @param fileExtension the extension of the new file
	 * @return a new temp file
	 * @throws FileOperationException if an error occurs creating the temp file
	 */
	private static File createTempFile(String fileExtension) throws FileOperationException {
		try {
			return File.createTempFile("Stickerify-", "." + fileExtension);
		} catch (IOException e) {
			throw new FileOperationException("An error occurred creating a new temp file", e);
		}
	}

	/**
	 * Deletes passed-in {@code file}.
	 *
	 * @param file the file to be removed
	 * @throws FileOperationException if an error occurs deleting the file
	 */
	private static void deleteFile(File file) throws FileOperationException {
		try {
			if (!Files.deleteIfExists(file.toPath())) {
				LOGGER.atInfo().log("Unable to delete file {}", file.toPath());
			}
		} catch (IOException e) {
			throw new FileOperationException("An error occurred deleting the file", e);
		}
	}

	/**
	 * Given a video file, it converts it to a WebM file of the proper dimension (max 512 x 512),
	 * based on the requirements specified by <a href="https://core.telegram.org/stickers/webm-vp9-encoding">Telegram documentation</a>.
	 *
	 * @param file the file to convert
	 * @return converted video
	 * @throws MediaException if file conversion is not successful
	 * @throws InterruptedException if the current thread is interrupted while converting the video file
	 */
	private static File convertToWebm(File file) throws MediaException, InterruptedException {
		var webmVideo = createTempFile("webm");
		var logPrefix = webmVideo.getAbsolutePath() + "-passlog";
		var baseCommand = new String[] {
				"ffmpeg",
				"-y",
				"-v", "error",
				"-i", file.getAbsolutePath(),
				"-vf", "scale='if(gt(iw,ih),%1$d,%2$d)':'if(gt(iw,ih),%2$d,%1$d)',fps='min(%3$d,source_fps)'".formatted(MAX_SIDE_LENGTH, VIDEO_KEEP_ASPECT_RATIO, MAX_VIDEO_FRAMES),
				"-c:v", "libvpx-" + VP9_CODEC,
				"-b:v", "650K",
				"-pix_fmt", "yuv420p",
				"-t", String.valueOf(MAX_VIDEO_DURATION_MILLIS / 1000),
				"-an",
				"-passlogfile", logPrefix
		};

		try {
			ProcessHelper.executeCommand(buildFfmpegCommand(baseCommand, "-pass", "1", "-f", "webm", OsConstants.NULL_FILE));
			ProcessHelper.executeCommand(buildFfmpegCommand(baseCommand, "-pass", "2", webmVideo.getAbsolutePath()));
		} catch (ProcessException e) {
			try {
				deleteFile(webmVideo);
			} catch (FileOperationException ex) {
				e.addSuppressed(ex);
			}
			throw new MediaException("FFmpeg two-pass conversion failed", e);
		} finally {
			var logFileName = logPrefix + "-0.log";
			try {
				deleteFile(new File(logFileName));
			} catch (FileOperationException e) {
				LOGGER.atWarn().setCause(e).log("Could not delete {}", logFileName);
			}
		}

		return webmVideo;
	}

	/**
	 * Builds the ffmpeg command combining a base part with a specific part (useful for 2 pass processing)
	 *
	 * @param baseCommand the common ffmpeg command
	 * @param additionalOptions command specific options
	 * @return the complete ffmpeg invocation command
	 */
	private static String[] buildFfmpegCommand(String[] baseCommand, String... additionalOptions) {
		var commands = new String[baseCommand.length + additionalOptions.length];
		System.arraycopy(baseCommand, 0, commands, 0, baseCommand.length);
		System.arraycopy(additionalOptions, 0, commands, baseCommand.length, additionalOptions.length);
		return commands;
	}

	private MediaHelper() {
		throw new UnsupportedOperationException();
	}
}
