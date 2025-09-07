package com.github.stickerifier.stickerify.process;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.stickerifier.stickerify.exception.ProcessException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public final class ProcessHelper {

	private static final Semaphore SEMAPHORE = new Semaphore(getMaxConcurrentProcesses());

	/**
	 * Executes passed-in command and ensures it completed successfully.
	 * Based on the operating system, the method limits the number of processes running concurrently:
	 * on Windows they will be at most 4, on every other system they will be at most 5.
	 *
	 * @param command the command to be executed
	 * @return the output of the command, split by lines
	 * @throws ProcessException either if:
	 * <ul>
	 *     <li>the command was unsuccessful
	 *     <li>an unexpected failure happened running the command
	 *     <li>an unexpected failure happened reading the output
	 * </ul>
	 * @throws InterruptedException if the current thread is interrupted while waiting for the command to finish execution
	 */
	public static List<String> executeCommand(final String... command) throws ProcessException, InterruptedException {
		SEMAPHORE.acquire();
		try {
			var process = new ProcessBuilder(command).redirectErrorStream(true).start();

			var output = new LinkedList<String>();
			try (var reader = process.inputReader(UTF_8)) {
				reader.lines().forEach(output::add);
			}

			var exitCode = process.waitFor();
			if (exitCode != 0) {
				process.destroy();
				var lines = String.join("\n", output);
				throw new ProcessException("The command {} exited with code {}\n{}", command[0], exitCode, lines);
			}

			return output;
		} catch (IOException e) {
			throw new ProcessException(e);
		} finally {
			SEMAPHORE.release();
		}
	}

	private static int getMaxConcurrentProcesses() {
		var value = System.getenv("CONCURRENT_PROCESSES");
		return value == null ? 4 : Integer.parseInt(value);
	}

	private ProcessHelper() {
		throw new UnsupportedOperationException();
	}
}
