package com.cellar.stickerify.bot;

import static com.cellar.stickerify.telegram.Answer.ERROR;
import static com.cellar.stickerify.telegram.Answer.FILE_READY;

import com.cellar.stickerify.image.ImageHelper;
import com.cellar.stickerify.telegram.Answer;
import com.cellar.stickerify.telegram.model.TelegramRequest;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Telegram bot to convert images in the format required to be used as Telegram stickers.
 *
 * @author Roberto Cella
 */
public class Stickerify extends TelegramLongPollingBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(Stickerify.class);

	@Override
	public String getBotUsername() {
		return "Stickerify";
	}

	@Override
	public String getBotToken() {
		return System.getenv("STICKERIFY_TOKEN");
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage()) {
			TelegramRequest request = new TelegramRequest(update.getMessage());
			LOGGER.info("Received {}", request);

			answer(request);
		}
	}

	private void answer(TelegramRequest request) {
		if (request.hasFile()) {
			answerFile(request);
		} else {
			answerText(request.getAnswerMessage(), request);
		}
	}

	private void answerFile(TelegramRequest request) {
		// TODO: change to HashSet.newHashSet(2) as soon as Gradle supports Java 19
		Set<Path> pathsToDelete = new HashSet<>(2);

		GetFile getFile = new GetFile(request.getFileId());

		try {
			File originalFile = downloadFile(execute(getFile).getFilePath());
			pathsToDelete.add(originalFile.toPath());

			File outputFile = ImageHelper.convertToPng(originalFile);
			pathsToDelete.add(outputFile.toPath());

			SendDocument response = SendDocument.builder()
					.chatId(request.getChatId())
					.replyToMessageId(request.getMessageId())
					.caption(FILE_READY.getText())
					.parseMode(ParseMode.MARKDOWNV2)
					.document(new InputFile(outputFile))
					.build();

			execute(response);
		} catch (TelegramApiException e) {
			LOGGER.warn("Unable to reply to {} with processed file", request, e);
			answerText(ERROR, request);
		} finally {
			deleteTempFiles(pathsToDelete);
		}
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
