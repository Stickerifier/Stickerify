package com.github.stickerifier.stickerify.process;

import com.github.stickerifier.stickerify.telegram.exception.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.process.ProcessLocator;

/**
 * Custom locator class to be used by Jave to find the path where FFmpeg is installed at in the system.
 *
 * @see ProcessLocator
 */
public class PathLocator implements ProcessLocator {

	private static final Logger LOGGER = LoggerFactory.getLogger(PathLocator.class);

	private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");
	private static final String[] FIND_FFMPEG = { IS_WINDOWS ? "where" : "which", "ffmpeg" };

	private String ffmpegLocation;

	public PathLocator() {
		try {
			ffmpegLocation = ProcessHelper.executeCommand(FIND_FFMPEG).split(System.lineSeparator())[0];

			LOGGER.atInfo().log("FFmpeg is installed at {}", ffmpegLocation);
		} catch (TelegramApiException e) {
			LOGGER.atError().setCause(e).log("Unable to detect FFmpeg's installation path");
		}
	}

	@Override
	public String getExecutablePath() {
		return ffmpegLocation;
	}
}
