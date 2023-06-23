package com.github.stickerifier.stickerify.process;

import static java.util.concurrent.TimeUnit.MINUTES;

import com.github.stickerifier.stickerify.telegram.exception.TelegramApiException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ProcessHelper {

	/**
	 * Executes passed-in command and ensures it completed successfully.
	 *
	 * @param command the command to be executed
	 * @return the instance of the process executed
	 * @throws TelegramApiException either if:
	 * <ul>
	 *     <li>the command was unsuccessful
	 *     <li>the waiting time elapsed
	 *     <li>an unexpected failure happened running the command
	 * </ul>
	 */
	public static Process executeCommand(final String[] command) throws TelegramApiException {
		try {
			var process = new ProcessBuilder(command).start();
			var processExited = process.waitFor(1, MINUTES);

			if (!processExited || process.exitValue() != 0) {
				throw new TelegramApiException("The command couldn't complete successfully");
			}

			return process;
		} catch (IOException | InterruptedException e) {
			throw new TelegramApiException(e);
		}
	}

	/**
	 * Executes passed-in command and returns its output.
	 *
	 * @param command the command to be executed
	 * @return the output of the command
	 * @throws TelegramApiException if the output of the command couldn't be retrieved
	 * @see ProcessHelper#executeCommand(String[])
	 */
	static String getCommandOutput(final String[] command) throws TelegramApiException {
		var process = executeCommand(command);

		try {
			return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new TelegramApiException(e);
		}
	}

	private ProcessHelper() {
		throw new UnsupportedOperationException();
	}
}
