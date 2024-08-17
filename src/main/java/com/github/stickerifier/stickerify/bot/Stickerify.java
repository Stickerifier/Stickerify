package com.github.stickerifier.stickerify.bot;

import static com.github.stickerifier.stickerify.telegram.Answer.CORRUPTED;
import static com.github.stickerifier.stickerify.telegram.Answer.ERROR;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_ALREADY_VALID;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_READY;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_TOO_LARGE;
import static com.pengrad.telegrambot.model.request.ParseMode.MarkdownV2;
import static java.util.HashSet.newHashSet;
import static java.util.concurrent.Executors.newFixedThreadPool;

import com.github.stickerifier.stickerify.exception.BaseException;
import com.github.stickerifier.stickerify.exception.CorruptedVideoException;
import com.github.stickerifier.stickerify.exception.FileOperationException;
import com.github.stickerifier.stickerify.exception.MediaException;
import com.github.stickerifier.stickerify.exception.TelegramApiException;
import com.github.stickerifier.stickerify.media.MediaHelper;
import com.github.stickerifier.stickerify.telegram.Answer;
import com.github.stickerifier.stickerify.telegram.model.TelegramFile;
import com.github.stickerifier.stickerify.telegram.model.TelegramRequest;
import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * Telegram bot to convert medias in the format required to be used as Telegram stickers.
 *
 * @author Roberto Cella
 */
public class Stickerify {

	private static final Logger LOGGER = LoggerFactory.getLogger(Stickerify.class);
	private static final String BOT_TOKEN = System.getenv("STICKERIFY_TOKEN");
	private static final ThreadFactory VIRTUAL_THREAD_FACTORY = Thread.ofVirtual().name("Virtual-", 0).factory();

	private final TelegramBot bot;
	private final Executor executor;

	/**
	 * Instantiate the bot processing requests with virtual threads.
	 *
	 * @see Stickerify
	 */
	public Stickerify() {
		this(new TelegramBot.Builder(BOT_TOKEN).updateListenerSleep(500).build(), newFixedThreadPool(getMaxConcurrentThreads(), VIRTUAL_THREAD_FACTORY));
	}

	/**
	 * Instantiate the bot processing requests with an arbitrary executor.
	 *
	 * @see Stickerify
	 */
	Stickerify(TelegramBot bot, Executor executor) {
		this.bot = bot;
		this.executor = executor;

		ExceptionHandler exceptionHandler = e -> LOGGER.atError().log("There was an unexpected failure: {}", e.getMessage());

		bot.setUpdatesListener(this::handleUpdates, exceptionHandler, new GetUpdates().timeout(50));
	}

	private int handleUpdates(List<Update> updates) {
		updates.forEach(update -> executor.execute(() -> {
			if (update.message() != null) {
				var request = new TelegramRequest(update.message());
				LOGGER.atInfo().log("Received {}", request.getDescription());

				answer(request);
			}
		}));

		return UpdatesListener.CONFIRMED_UPDATES_ALL;
	}

	private void answer(TelegramRequest request) {
		var file = request.getFile();

		if (file != null) {
			answerFile(request, file);
		} else {
			answerText(request);
		}
	}

	private void answerFile(TelegramRequest request, TelegramFile file) {
		if (file == TelegramFile.NOT_SUPPORTED) {
			answerText(ERROR, request);
		} else if (file.canBeDownloaded()) {
			answerFile(request, file.id());
		} else {
			LOGGER.atInfo().log("Passed-in file is too large");

			answerText(FILE_TOO_LARGE, request);
		}
	}

	private void answerFile(TelegramRequest request, String fileId) {
		Set<Path> pathsToDelete = newHashSet(2);

		try {
			var originalFile = retrieveFile(fileId);
			pathsToDelete.add(originalFile.toPath());

			LOGGER.atTrace().log("Converting file {}", fileId);
			var outputFile = MediaHelper.convert(originalFile);
			LOGGER.atTrace().log("File converted successfully");

			if (outputFile == null) {
				answerText(FILE_ALREADY_VALID, request);
			} else {
				pathsToDelete.add(outputFile.toPath());

				var answerWithFile = new SendDocument(request.getChatId(), outputFile)
						.replyToMessageId(request.getMessageId())
						.disableContentTypeDetection(true)
						.caption(FILE_READY.getText())
						.parseMode(MarkdownV2);

				execute(answerWithFile);
			}
		} catch (TelegramApiException | MediaException e) {
			processFailure(request, e);
		} finally {
			deleteTempFiles(pathsToDelete);
		}
	}

	private File retrieveFile(String fileId) throws TelegramApiException, FileOperationException {
		var file = execute(new GetFile(fileId)).file();

		try {
			var fileContent = bot.getFileContent(file);
			var downloadedFile = File.createTempFile("OriginalFile-", null);
			Files.write(downloadedFile.toPath(), fileContent);

			return downloadedFile;
		} catch (IOException e) {
			throw new FileOperationException(e);
		}
	}

	private void processFailure(TelegramRequest request, BaseException e) {
		if (e instanceof TelegramApiException telegramException) {
			processTelegramFailure(request.getDescription(), telegramException, false);
		}

		if (e instanceof CorruptedVideoException) {
			LOGGER.atInfo().log("Unable to reply to the {}: the file is corrupted", request.getDescription());
			answerText(CORRUPTED, request);
		} else {
			LOGGER.atWarn().setCause(e).log("Unable to process the file {}", request.getFile().id());
			answerText(ERROR, request);
		}
	}

	private void processTelegramFailure(String requestDescription, TelegramApiException e, boolean logUnmatchedFailure) {
		var exceptionMessage = e.getMessage();

		if (exceptionMessage.endsWith("Bad Request: message to be replied not found")) {
			LOGGER.atInfo().log("Unable to reply to the {}: the message sent has been deleted", requestDescription);
		} else if (exceptionMessage.endsWith("Forbidden: bot was blocked by the user")) {
			LOGGER.atInfo().log("Unable to reply to the {}: the user blocked the bot", requestDescription);
		} else if (logUnmatchedFailure) {
			LOGGER.atError().setCause(e).log("Unable to reply to the {}", requestDescription);
		}
	}

	private void answerText(TelegramRequest request) {
		var message = request.message();
		if (message.text() == null) {
			LOGGER.atInfo().log("An unhandled message type has been received: {}", message);
		}

		answerText(request.getAnswerMessage(), request);
	}

	private void answerText(Answer answer, TelegramRequest request) {
		var previewOptions = new LinkPreviewOptions().isDisabled(answer.isDisableLinkPreview());

		var answerWithText = new SendMessage(request.getChatId(), answer.getText())
				.replyToMessageId(request.getMessageId())
				.parseMode(MarkdownV2)
				.linkPreviewOptions(previewOptions);

		try {
			execute(answerWithText);
		} catch (TelegramApiException e) {
			processTelegramFailure(request.getDescription(), e, true);
		}
	}

	private <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request) throws TelegramApiException {
		LOGGER.atTrace().log("Sending {} request", request.getMethod());

		var response = bot.execute(request);

		if (response.isOk()) {
			return response;
		}

		throw new TelegramApiException("Telegram couldn't execute the {} request: {}", request.getMethod(), response.description());
	}

	private static void deleteTempFiles(Set<Path> pathsToDelete) {
		for (var path : pathsToDelete) {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				LOGGER.atError().setCause(e).log("An error occurred trying to delete temp file {}", path);
			}
		}
	}

	private static int getMaxConcurrentThreads() {
		var value = System.getenv("CONCURRENT_THREADS");
		return value == null ? 5 : Integer.parseInt(value);
	}
}
