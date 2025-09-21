package com.github.stickerifier.stickerify.bot;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.stickerifier.stickerify.junit.ClearTempFiles;
import com.github.stickerifier.stickerify.junit.Tags;
import com.github.stickerifier.stickerify.telegram.Answer;
import com.pengrad.telegrambot.TelegramBot;
import mockwebserver3.MockWebServer;
import mockwebserver3.QueueDispatcher;
import mockwebserver3.RecordedRequest;
import mockwebserver3.junit5.StartStop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;

@Tag(Tags.TELEGRAM_API)
@ClearTempFiles
class StickerifyTest {

	@StartStop
	private final MockWebServer server = new MockWebServer();

	@BeforeEach
	public void setup() {
		((QueueDispatcher) server.getDispatcher()).setFailFast(MockResponses.EMPTY_UPDATES);
	}

	@Test
	void startMessage() throws Exception {
		server.enqueue(MockResponses.START_MESSAGE);

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var sendMessage = server.takeRequest();
			assertEquals("/api/token/sendMessage", sendMessage.getTarget());
			assertResponseContainsMessage(sendMessage, Answer.HELP);
		}
	}

	private Stickerify runBot() {
		var bot = new TelegramBot.Builder("token")
				.apiUrl(server.url("api/").toString())
				.fileApiUrl(server.url("files/").toString())
				.updateListenerSleep(500)
				.build();

		return new Stickerify(bot, Runnable::run);
	}

	private static void assertResponseContainsMessage(RecordedRequest request, Answer answer) {
		var message = URLEncoder.encode(answer.getText(), UTF_8);
		assertNotNull(request.getBody());
		assertThat(request.getBody().utf8(), containsString(message));
	}

	@Test
	void helpMessage() throws Exception {
		server.enqueue(MockResponses.HELP_MESSAGE);

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var sendMessage = server.takeRequest();
			assertEquals("/api/token/sendMessage", sendMessage.getTarget());
			assertResponseContainsMessage(sendMessage, Answer.HELP);
		}
	}

	@Test
	void privacyMessage() throws Exception {
		server.enqueue(MockResponses.PRIVACY_MESSAGE);

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var sendMessage = server.takeRequest();
			assertEquals("/api/token/sendMessage", sendMessage.getTarget());
			assertResponseContainsMessage(sendMessage, Answer.PRIVACY_POLICY);
		}
	}

	@Test
	void fileNotSupported() throws Exception {
		server.enqueue(MockResponses.FILE_NOT_SUPPORTED);

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var sendMessage = server.takeRequest();
			assertEquals("/api/token/sendMessage", sendMessage.getTarget());
			assertResponseContainsMessage(sendMessage, Answer.ERROR);
		}
	}

	@Test
	void fileTooBig() throws Exception {
		server.enqueue(MockResponses.FILE_TOO_BIG);

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var sendMessage = server.takeRequest();
			assertEquals("/api/token/sendMessage", sendMessage.getTarget());
			assertResponseContainsMessage(sendMessage, Answer.FILE_TOO_LARGE);
		}
	}

	@Test
	void fileAlreadyValid() throws Exception {
		server.enqueue(MockResponses.ANIMATED_STICKER);
		server.enqueue(MockResponses.fileInfo("animated_sticker.tgs"));
		server.enqueue(MockResponses.fileDownload("animated_sticker.tgs"));

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var getFile = server.takeRequest();
			assertEquals("/api/token/getFile", getFile.getTarget());
			assertNotNull(getFile.getBody());
			assertEquals("file_id=animated_sticker.tgs", getFile.getBody().utf8());

			var download = server.takeRequest();
			assertEquals("/files/token/animated_sticker.tgs", download.getTarget());

			var sendMessage = server.takeRequest();
			assertEquals("/api/token/sendMessage", sendMessage.getTarget());
			assertResponseContainsMessage(sendMessage, Answer.FILE_ALREADY_VALID);
		}
	}

	@Test
	void convertedPng() throws Exception {
		server.enqueue(MockResponses.PNG_FILE);
		server.enqueue(MockResponses.fileInfo("big.png"));
		server.enqueue(MockResponses.fileDownload("big.png"));

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var getFile = server.takeRequest();
			assertEquals("/api/token/getFile", getFile.getTarget());
			assertNotNull(getFile.getBody());
			assertEquals("file_id=big.png", getFile.getBody().utf8());

			var download = server.takeRequest();
			assertEquals("/files/token/big.png", download.getTarget());

			var sendDocument = server.takeRequest();
			assertEquals("/api/token/sendDocument", sendDocument.getTarget());
			assertNotNull(sendDocument.getBody());
			assertThat(sendDocument.getBody().utf8(), containsString(Answer.FILE_READY.getText()));
		}
	}

	@Test
	void convertedWebp() throws Exception {
		server.enqueue(MockResponses.WEBP_FILE);
		server.enqueue(MockResponses.fileInfo("static.webp"));
		server.enqueue(MockResponses.fileDownload("static.webp"));

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var getFile = server.takeRequest();
			assertEquals("/api/token/getFile", getFile.getTarget());
			assertNotNull(getFile.getBody());
			assertEquals("file_id=static.webp", getFile.getBody().utf8());

			var download = server.takeRequest();
			assertEquals("/files/token/static.webp", download.getTarget());

			var sendDocument = server.takeRequest();
			assertEquals("/api/token/sendDocument", sendDocument.getTarget());
			assertNotNull(sendDocument.getBody());
			assertThat(sendDocument.getBody().utf8(), containsString(Answer.FILE_READY.getText()));
		}
	}

	@Test
	void convertedMov() throws Exception {
		server.enqueue(MockResponses.MOV_FILE);
		server.enqueue(MockResponses.fileInfo("long.mov"));
		server.enqueue(MockResponses.fileDownload("long.mov"));

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var getFile = server.takeRequest();
			assertEquals("/api/token/getFile", getFile.getTarget());
			assertNotNull(getFile.getBody());
			assertEquals("file_id=long.mov", getFile.getBody().utf8());

			var download = server.takeRequest();
			assertEquals("/files/token/long.mov", download.getTarget());

			var sendDocument = server.takeRequest();
			assertEquals("/api/token/sendDocument", sendDocument.getTarget());
			assertNotNull(sendDocument.getBody());
			assertThat(sendDocument.getBody().utf8(), containsString(Answer.FILE_READY.getText()));
		}
	}

	@Test
	void convertedWebm() throws Exception {
		server.enqueue(MockResponses.WEBM_FILE);
		server.enqueue(MockResponses.fileInfo("short_low_fps.webm"));
		server.enqueue(MockResponses.fileDownload("short_low_fps.webm"));

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var getFile = server.takeRequest();
			assertEquals("/api/token/getFile", getFile.getTarget());
			assertNotNull(getFile.getBody());
			assertEquals("file_id=short_low_fps.webm", getFile.getBody().utf8());

			var download = server.takeRequest();
			assertEquals("/files/token/short_low_fps.webm", download.getTarget());

			var sendDocument = server.takeRequest();
			assertEquals("/api/token/sendDocument", sendDocument.getTarget());
			assertNotNull(sendDocument.getBody());
			assertThat(sendDocument.getBody().utf8(), containsString(Answer.FILE_READY.getText()));
		}
	}

	@Test
	void convertedGif() throws Exception {
		server.enqueue(MockResponses.GIF_FILE);
		server.enqueue(MockResponses.fileInfo("valid.gif"));
		server.enqueue(MockResponses.fileDownload("valid.gif"));

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var getFile = server.takeRequest();
			assertEquals("/api/token/getFile", getFile.getTarget());
			assertNotNull(getFile.getBody());
			assertEquals("file_id=valid.gif", getFile.getBody().utf8());

			var download = server.takeRequest();
			assertEquals("/files/token/valid.gif", download.getTarget());

			var sendDocument = server.takeRequest();
			assertEquals("/api/token/sendDocument", sendDocument.getTarget());
			assertNotNull(sendDocument.getBody());
			assertThat(sendDocument.getBody().utf8(), containsString(Answer.FILE_READY.getText()));
		}
	}

	@Test
	void documentNotSupported() throws Exception {
		server.enqueue(MockResponses.DOCUMENT);
		server.enqueue(MockResponses.fileInfo("document.txt"));
		server.enqueue(MockResponses.fileDownload("document.txt"));

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var getFile = server.takeRequest();
			assertEquals("/api/token/getFile", getFile.getTarget());
			assertNotNull(getFile.getBody());
			assertEquals("file_id=document.txt", getFile.getBody().utf8());

			var download = server.takeRequest();
			assertEquals("/files/token/document.txt", download.getTarget());

			var sendMessage = server.takeRequest();
			assertEquals("/api/token/sendMessage", sendMessage.getTarget());
			assertResponseContainsMessage(sendMessage, Answer.ERROR);
		}
	}

	@Test
	void corruptedVideo() throws Exception {
		server.enqueue(MockResponses.CORRUPTED_FILE);
		server.enqueue(MockResponses.fileInfo("corrupted.mp4"));
		server.enqueue(MockResponses.fileDownload("corrupted.mp4"));

		try (var _ = runBot()) {
			var getUpdates = server.takeRequest();
			assertEquals("/api/token/getUpdates", getUpdates.getTarget());

			var getFile = server.takeRequest();
			assertEquals("/api/token/getFile", getFile.getTarget());
			assertNotNull(getFile.getBody());
			assertEquals("file_id=corrupted.mp4", getFile.getBody().utf8());

			var download = server.takeRequest();
			assertEquals("/files/token/corrupted.mp4", download.getTarget());

			var sendMessage = server.takeRequest();
			assertEquals("/api/token/sendMessage", sendMessage.getTarget());
			assertResponseContainsMessage(sendMessage, Answer.CORRUPTED);
		}
	}
}
