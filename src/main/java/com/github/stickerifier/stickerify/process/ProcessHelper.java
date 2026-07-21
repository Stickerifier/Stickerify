package com.github.stickerifier.stickerify.process;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.stickerifier.stickerify.exception.ProcessException;
import com.github.stickerifier.stickerify.logger.StructuredLogger;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.StringJoiner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class ProcessHelper {

	private static final StructuredLogger LOGGER = new StructuredLogger(ProcessHelper.class);
	private static final Semaphore SEMAPHORE = new Semaphore(getMaxConcurrentProcesses());

	/**
	 * Executes passed-in command and ensures it completed successfully.
	 * Concurrency is limited by a process-wide semaphore sized by the {@code CONCURRENT_PROCESSES}
	 * environment variable (defaults to 4).
	 *
	 * @param command the command to be executed
	 * @return the standard output of the command
	 * @throws ProcessException either if:
	 * <ul>
	 *     <li>the command was unsuccessful
	 *     <li>the waiting time elapsed
	 *     <li>an unexpected failure happened running the command
	 *     <li>an unexpected failure happened reading the output
	 * </ul>
	 * @throws InterruptedException if the current thread is interrupted while waiting for the command to finish
	 */
	public static String executeCommand(final String... command) throws ProcessException, InterruptedException {
		SEMAPHORE.acquire();

		try (var process = new ProcessBuilder(command).start()) {
			var standardOutput = new StringJoiner("\n");
			var outputThread = Thread.ofVirtual().start(() -> {
				try (var reader = process.inputReader(UTF_8)) {
					reader.lines().forEach(standardOutput::add);
				} catch (IOException | UncheckedIOException e) {
					LOGGER.at(Level.ERROR).setCause(e).log("An error occurred using process output reader");
				}
			});

			var standardError = new StringJoiner("\n");
			var errorThread = Thread.ofVirtual().start(() -> {
				try (var reader = process.errorReader(UTF_8)) {
					reader.lines().forEach(standardError::add);
				} catch (IOException | UncheckedIOException e) {
					LOGGER.at(Level.ERROR).setCause(e).log("An error occurred using process error reader");
				}
			});

			var finished = process.waitFor(1, TimeUnit.MINUTES);
			if (!finished) {
				process.destroyForcibly();
				outputThread.join();
				errorThread.join();
				LOGGER.at(Level.WARN).log("The command {} timed out after 1m: {}", command[0], standardError.toString());
				throw new ProcessException("The command {} timed out after 1m", command[0]);
			}

			outputThread.join();
			errorThread.join();

			var exitCode = process.exitValue();
			if (exitCode != 0) {
				LOGGER.at(Level.WARN).log("The command {} exited with code {}: {}", command[0], exitCode, standardError.toString());
				throw new ProcessException("The command {} exited with code {}", command[0], exitCode);
			}

			return standardOutput.toString();
		} catch (IOException e) {
			throw new ProcessException(e);
		} finally {
			SEMAPHORE.release();
		}
	}

	private static int getMaxConcurrentProcesses() {
		var concurrentProcesses = System.getenv("CONCURRENT_PROCESSES");
		var value = concurrentProcesses == null ? 4 : Integer.parseInt(concurrentProcesses);
		if (value < 1) {
			throw new IllegalArgumentException("The environment variable CONCURRENT_PROCESSES must be >= 1 (was " + concurrentProcesses + ")");
		}

		return value;
	}

	private ProcessHelper() {
		throw new UnsupportedOperationException();
	}
}
