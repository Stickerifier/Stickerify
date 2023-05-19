package com.github.stickerifier.stickerify.bot;

import static com.github.stickerifier.stickerify.telegram.Answer.ERROR;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_ALREADY_VALID;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_READY;
import static com.pengrad.telegrambot.model.request.ParseMode.MarkdownV2;
import static java.util.HashSet.newHashSet;

import com.github.stickerifier.stickerify.media.MediaHelper;
import com.github.stickerifier.stickerify.telegram.Answer;
import com.github.stickerifier.stickerify.telegram.exception.TelegramApiException;
import com.github.stickerifier.stickerify.telegram.model.TelegramRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Telegram bot to convert medias in the format required to be used as Telegram stickers.
 *
 * @author Roberto Cella
 */
public class Stickerify extends TelegramBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(Stickerify.class);
	private static final String BOT_TOKEN = System.getenv("STICKERIFY_TOKEN");

	/**
	 * @see Stickerify
	 */
	public Stickerify() {
		super(BOT_TOKEN);

		setUpdatesListener(updates -> {
			updates.forEach(this::handleUpdate);
			return UpdatesListener.CONFIRMED_UPDATES_ALL;
		}, e -> LOGGER.error("There was an unexpected failure: {}", e.getMessage()));
	}

	private void handleUpdate(Update update) {
		if (update.message() != null) {
			var request = new TelegramRequest(update.message());
			LOGGER.info("Received {}", request.getDescription());

			answer(request);
		}
	}

	private void answer(TelegramRequest request) {
		if (request.hasFile()) {
			answerFile(request);
		} else {
			answerText(request);
		}
	}

	private void answerFile(TelegramRequest request) {
		Set<Path> pathsToDelete = newHashSet(2);

		try {
			File originalFile = retrieveFile(request.getFileId());
			pathsToDelete.add(originalFile.toPath());

			File outputFile = MediaHelper.convert(originalFile);

			if (outputFile == null) {
				answerText(FILE_ALREADY_VALID, request);
			} else {
				pathsToDelete.add(outputFile.toPath());

				var answerWithFile = new SendDocument(request.getChatId(), outputFile)
						.replyToMessageId(request.getMessageId())
						.caption(FILE_READY.getText())
						.parseMode(MarkdownV2);

				if (!execute(answerWithFile).isOk()) {
					throw new TelegramApiException("Telegram failed to reply with processed file");
				}
			}
		} catch (TelegramApiException e) {
			LOGGER.warn("Unable to reply to {} with processed file", request.getDescription(), e);
			answerText(ERROR, request);
		} finally {
			deleteTempFiles(pathsToDelete);
		}
	}

	private File retrieveFile(String fileId) throws TelegramApiException {
		var file = execute(new GetFile(fileId)).file();

		try {
			var fileContent = getFileContent(file);
			var downloadedFile = File.createTempFile("OriginalFile-", null);
			Files.write(downloadedFile.toPath(), fileContent);

			return downloadedFile;
		} catch (IOException e) {
			throw new TelegramApiException(e);
		}
	}

	private void answerText(TelegramRequest request) {
		answerText(request.getAnswerMessage(), request);
	}

	private void answerText(Answer answer, TelegramRequest request) {
		var answerWithText = new SendMessage(request.getChatId(), answer.getText())
				.parseMode(MarkdownV2)
				.disableWebPagePreview(answer.isDisableWebPreview());

		if (!execute(answerWithText).isOk()) {
			LOGGER.error("Unable to reply to {} with {}", request, answerWithText);
		}
	}

	private static void deleteTempFiles(Set<Path> pathsToDelete) {
		for (var path : pathsToDelete) {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				LOGGER.error("An error occurred trying to delete temp file {}", path, e);
			}
		}
	}
}
