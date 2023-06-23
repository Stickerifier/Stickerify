package com.github.stickerifier.stickerify.process;

import com.github.stickerifier.stickerify.telegram.exception.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.process.ProcessLocator;

public class SystemFfmpegLocator implements ProcessLocator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemFfmpegLocator.class);

	private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("Windows");
	private static final String[] FIND_FFMPEG = { IS_WINDOWS ? "where" : "which", "ffmpeg" };

	private static String ffmpegLocation = "";

	@Override
	public String getExecutablePath() {

		if (ffmpegLocation.isEmpty()) {
			try {
				ffmpegLocation = ProcessHelper.getCommandOutput(FIND_FFMPEG).trim();

				LOGGER.atInfo().log("FFmpeg is installed at {}", ffmpegLocation);
			} catch (TelegramApiException e) {
				LOGGER.atError().setCause(e).log("Unable to detect the installation path of FFmpeg");
			}
		}

		return ffmpegLocation;
	}
}
