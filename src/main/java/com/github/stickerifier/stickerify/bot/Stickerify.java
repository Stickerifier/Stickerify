package com.github.stickerifier.stickerify.bot;

import static com.github.stickerifier.stickerify.telegram.Answer.ERROR;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_ALREADY_VALID;
import static com.github.stickerifier.stickerify.telegram.Answer.FILE_READY;
import static java.util.HashSet.newHashSet;

import com.github.stickerifier.stickerify.media.MediaHelper;
import com.github.stickerifier.stickerify.telegram.Answer;
import com.github.stickerifier.stickerify.telegram.model.TelegramRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
public class Stickerify extends TelegramLongPollingBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(Stickerify.class);

	public Stickerify() {
		super(System.getenv("STICKERIFY_TOKEN"));
	}

	@Override
	public String getBotUsername() {
		return Stickerify.class.getSimpleName();
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage()) {
			TelegramRequest request = new TelegramRequest(update.getMessage());
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

				SendDocument response = SendDocument.builder()
						.chatId(request.getChatId())
						.replyToMessageId(request.getMessageId())
						.caption(FILE_READY.getText())
						.parseMode(ParseMode.MARKDOWNV2)
						.document(new InputFile(outputFile))
						.build();

				execute(response);
			}
		} catch (TelegramApiException e) {
			LOGGER.warn("Unable to reply to {} with processed file", request, e);
			answerText(ERROR, request);
		} finally {
			deleteTempFiles(pathsToDelete);
		}
	}

	private File retrieveFile(String fileId) throws TelegramApiException {
		GetFile getFile = new GetFile(fileId);

		return downloadFile(execute(getFile).getFilePath());
	}

	private void answerText(TelegramRequest request) {
		answerText(request.getAnswerMessage(), request);
	}

	private void answerText(Answer answer, TelegramRequest request) {
		SendMessage response = SendMessage.builder()
				.chatId(request.getChatId())
				.text(answer.getText())
				.parseMode(ParseMode.MARKDOWNV2)
				.disableWebPagePreview(answer.isDisableWebPreview())
				.build();

		try {
			execute(response);
		} catch (TelegramApiException e) {
			LOGGER.error("Unable to reply to {} with {}", request, response, e);
		}
	}

	private static void deleteTempFiles(Set<Path> pathsToDelete) {
		for (Path path : pathsToDelete) {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				LOGGER.error("An error occurred trying to delete temp file {}", path, e);
			}
		}
	}
}
