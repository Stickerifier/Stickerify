package com.github.stickerifier.stickerify.process;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.stickerifier.stickerify.exception.ProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class ProcessHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessHelper.class);
	private static final Semaphore SEMAPHORE = new Semaphore(getMaxConcurrentProcesses());

	/**
	 * Executes passed-in command and ensures it completed successfully.
	 * Concurrency is limited by a process-wide semaphore sized by the {@code CONCURRENT_PROCESSES}
	 * environment variable (defaults to 4).
	 *
	 * @param command the command to be executed
	 * @return the merged stdout/stderr of the command, split by lines
	 * @throws ProcessException either if:
	 * <ul>
	 *     <li>the command was unsuccessful
	 *     <li>the waiting time elapsed
	 *     <li>an unexpected failure happened running the command
	 *     <li>an unexpected failure happened reading the output
	 * </ul>
	 * @throws InterruptedException if the current thread is interrupted while waiting for the command to finish
	 */
	public static List<String> executeCommand(final String... command) throws ProcessException, InterruptedException {
		SEMAPHORE.acquire();
		try {
			var process = new ProcessBuilder(command).redirectErrorStream(true).start();

			var output = new ArrayList<String>(64);
			var readerThread = Thread.ofVirtual().start(() -> {
				try (var reader = process.inputReader(UTF_8)) {
					reader.lines().forEach(output::add);
				} catch (IOException e) {
					LOGGER.atError().setCause(e).log("Error while closing process output reader");
				}
			});

			var finished = process.waitFor(1, TimeUnit.MINUTES);
			if (!finished) {
				process.destroyForcibly();
				readerThread.join();
				throw new ProcessException("The command {} timed out after 1m\n{}", command[0], String.join("\n", output));
			}

			readerThread.join();
			var exitCode = process.exitValue();
			if (exitCode != 0) {
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
		var env = System.getenv("CONCURRENT_PROCESSES");
		var value = env == null ? 4 : Integer.parseInt(env);
		if (value < 1) {
			throw new IllegalArgumentException("CONCURRENT_PROCESSES must be >= 1 (was " + env + ")");
		}
		return value;
	}

	private ProcessHelper() {
		throw new UnsupportedOperationException();
	}
}
