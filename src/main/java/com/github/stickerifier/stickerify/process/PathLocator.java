package com.github.stickerifier.stickerify.process;

import com.github.stickerifier.stickerify.exception.ProcessException;
import org.slf4j.LoggerFactory;
import ws.schild.jave.process.ProcessLocator;
import ws.schild.jave.process.ProcessWrapper;
import ws.schild.jave.process.ffmpeg.FFMPEGProcess;

/**
 * Custom locator class to be used by Jave to find the path where FFmpeg is installed at in the system.
 *
 * @see ProcessLocator
 */
public enum PathLocator implements ProcessLocator {
	INSTANCE;

	private String ffmpegLocation = System.getenv("FFMPEG_PATH");

	PathLocator() {
		var logger = LoggerFactory.getLogger(PathLocator.class);
		try {
			if (ffmpegLocation == null || ffmpegLocation.isBlank()) {
				ffmpegLocation = ProcessHelper.executeCommand(OsConstants.FIND_EXECUTABLE, "ffmpeg").getFirst();
			}

			logger.atInfo().log("FFmpeg is installed at {}", ffmpegLocation);
		} catch (ProcessException e) {
			logger.atError().setCause(e).log("Unable to detect FFmpeg's installation path");
		} catch (InterruptedException _) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public String getExecutablePath() {
		return ffmpegLocation;
	}

	@Override
	public ProcessWrapper createExecutor() {
		return new FFMPEGProcess(getExecutablePath());
	}
}
