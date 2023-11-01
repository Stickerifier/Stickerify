package com.github.stickerifier.stickerify.process;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MINUTES;

import com.github.stickerifier.stickerify.telegram.exception.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

public final class ProcessHelper {

	static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
	private static final int MAX_CONCURRENT_PROCESSES = IS_WINDOWS ? 4 : 5;
	private static final Semaphore SEMAPHORE = new Semaphore(MAX_CONCURRENT_PROCESSES);

	/**
	 * Executes passed-in command and ensures it completed successfully.
	 * Based on the operating system, the method limits the number of processes running concurrently:
	 * on Windows they will be at most 4, on every other system they will be at most 5.
	 *
	 * @param command the command to be executed
	 * @return the output of the command
	 * @throws TelegramApiException either if:
	 * <ul>
	 *     <li>the command was unsuccessful
	 *     <li>the waiting time elapsed
	 *     <li>an unexpected failure happened running the command
	 * </ul>
	 */
	public static String executeCommand(final String[] command) throws TelegramApiException {
		Process process = null;

		try {
			SEMAPHORE.acquire();
			process = new ProcessBuilder(command).start();
			var processExited = process.waitFor(1, MINUTES);

			if (!processExited || process.exitValue() != 0) {
				var reason = processExited ? "successfully" : "in time";
				var output = readStream(process.getErrorStream());
				throw new TelegramApiException("The command {} couldn't complete {}: {}", command[0], reason, output);
			}

			return readStream(process.getInputStream());
		} catch (IOException | InterruptedException e) {
			throw new TelegramApiException(e);
		} finally {
			SEMAPHORE.release();
			if (process != null) {
				process.destroy();
			}
		}
	}

	private static String readStream(InputStream stream) throws IOException {
		return new String(stream.readAllBytes(), UTF_8);
	}

	private ProcessHelper() {
		throw new UnsupportedOperationException();
	}
}
