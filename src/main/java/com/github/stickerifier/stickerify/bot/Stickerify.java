package com.github.stickerifier.stickerify.bot;

import static com.github.stickerifier.stickerify.telegram.Answer.ABOUT;
import static com.github.stickerifier.stickerify.telegram.Answer.ERROR;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_ALREADY_VALID;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_READY;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_TOO_LARGE;
import static com.github.stickerifier.stickerify.telegram.Answer.HELP;
import static com.pengrad.telegrambot.model.request.ParseMode.MarkdownV2;
import static java.util.HashSet.newHashSet;

import com.github.stickerifier.stickerify.media.MediaHelper;
import com.github.stickerifier.stickerify.telegram.Answer;
import com.github.stickerifier.stickerify.telegram.exception.TelegramApiException;
import com.github.stickerifier.stickerify.telegram.model.TelegramRequest;
import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
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

/**
 * Telegram bot to convert medias in the format required to be used as Telegram stickers.
 *
 * @author Roberto Cella
 */
public class Stickerify {

	private static final Logger LOGGER = LoggerFactory.getLogger(Stickerify.class);
	private static final String BOT_TOKEN = System.getenv("STICKERIFY_TOKEN");

	private final TelegramBot bot = new TelegramBot.Builder(BOT_TOKEN).updateListenerSleep(500).build();

	/**
	 * @see Stickerify
	 */
	public Stickerify() {
		ExceptionHandler exceptionHandler = e -> LOGGER.atError().log("There was an unexpected failure: {}", e.getMessage());

		bot.setUpdatesListener(this::handleUpdates, exceptionHandler, new GetUpdates().timeout(50));
	}

	private int handleUpdates(List<Update> updates) {
		updates.forEach(update -> {
			if (update.message() != null) {
				var request = new TelegramRequest(update.message());
				LOGGER.atInfo().log("Received {}", request.getDescription());

				answer(request);
			}
		});

		return UpdatesListener.CONFIRMED_UPDATES_ALL;
	}

	private void answer(TelegramRequest request) {
		var file = request.getFile();
		if (file != null) {
			if (file.canBeDownloaded()) {
				answerFile(request, file.fileId());
			} else {
				answerText(FILE_TOO_LARGE, request);
			}
		} else {
			if (request.isHelpCommand()) {
				answerText(HELP, request);
			} else {
				answerText(ABOUT, request);
			}
		}
	}

	private void answerFile(TelegramRequest request, String fileId) {
		Set<Path> pathsToDelete = newHashSet(2);

		try {
			if (fileId == null) {
				throw new TelegramApiException("The request doesn't contain a supported media: {}", request.message());
			}

			var originalFile = retrieveFile(fileId);
			pathsToDelete.add(originalFile.toPath());

			var outputFile = MediaHelper.convert(originalFile);

			if (outputFile == null) {
				answerText(FILE_ALREADY_VALID, request);
			} else {
				pathsToDelete.add(outputFile.toPath());

				var answerWithFile = new SendDocument(request.getChatId(), outputFile)
						.replyToMessageId(request.getMessageId())
						.caption(FILE_READY.getText())
						.parseMode(MarkdownV2);

				execute(answerWithFile);
			}
		} catch (TelegramApiException e) {
			LOGGER.atWarn().setCause(e).log("Unable to reply to {} with processed file", request.getDescription());
			answerText(ERROR, request);
		} finally {
			deleteTempFiles(pathsToDelete);
		}
	}

	private File retrieveFile(String fileId) throws TelegramApiException {
		var file = execute(new GetFile(fileId)).file();

		try {
			var fileContent = bot.getFileContent(file);
			var downloadedFile = File.createTempFile("OriginalFile-", null);
			Files.write(downloadedFile.toPath(), fileContent);

			return downloadedFile;
		} catch (IOException e) {
			throw new TelegramApiException(e);
		}
	}

	private void answerText(Answer answer, TelegramRequest request) {
		var answerWithText = new SendMessage(request.getChatId(), answer.getText())
				.parseMode(MarkdownV2)
				.disableWebPagePreview(answer.isDisableWebPreview());

		try {
			execute(answerWithText);
		} catch (TelegramApiException e) {
			LOGGER.atError().log("Unable to reply to {} with {}", request, answerWithText);
		}
	}

	private <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request) throws TelegramApiException {
		var response = bot.execute(request);

		if (response.isOk()) {
			return response;
		}

		throw new TelegramApiException("Telegram couldn't execute the {} request", request.getMethod());
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
}
