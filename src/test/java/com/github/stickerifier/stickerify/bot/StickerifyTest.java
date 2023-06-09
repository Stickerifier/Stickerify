package com.github.stickerifier.stickerify.bot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.stickerifier.stickerify.media.ResourceHelper;
import com.github.stickerifier.stickerify.telegram.Answer;
import com.pengrad.telegrambot.TelegramBot;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class StickerifyTest {

	@TempDir
	private File directory;

	private ResourceHelper resources;

	public final MockWebServer server = new MockWebServer();

	@BeforeEach
	void setup() {
		resources = new ResourceHelper(directory);
	}

	@Test
	void startMessage() throws Exception {
		server.enqueue(MockResponses.START_MESSAGE);

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var sendMessage = server.takeRequest();
		assertEquals("/api/token/sendMessage", sendMessage.getPath());
		assertResponseContainsMessage(sendMessage, Answer.ABOUT);
	}

	private static void startBot(MockWebServer server) {
		var telegramBot = new TelegramBot.Builder("token")
				.apiUrl(server.url("api/").toString())
				.fileApiUrl(server.url("files/").toString())
				.updateListenerSleep(500)
				.build();
		new Stickerify(telegramBot);
	}

	private static void assertResponseContainsMessage(RecordedRequest request, Answer answer) {
		var message = URLEncoder.encode(answer.getText(), StandardCharsets.UTF_8).replace("+", "%20");
		assertThat(request.getBody().readUtf8(), containsString(message));
	}

	@Test
	void helpMessage() throws Exception {
		server.enqueue(MockResponses.HELP_MESSAGE);

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var sendMessage = server.takeRequest();
		assertEquals("/api/token/sendMessage", sendMessage.getPath());
		assertResponseContainsMessage(sendMessage, Answer.HELP);
	}

	@Test
	void fileNotSupported() throws Exception {
		server.enqueue(MockResponses.FILE_NOT_SUPPORTED);

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var sendMessage = server.takeRequest();
		assertEquals("/api/token/sendMessage", sendMessage.getPath());
		assertResponseContainsMessage(sendMessage, Answer.ERROR);
	}

	@Test
	void fileTooBig() throws Exception {
		server.enqueue(MockResponses.FILE_TOO_BIG);

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var sendMessage = server.takeRequest();
		assertEquals("/api/token/sendMessage", sendMessage.getPath());
		assertResponseContainsMessage(sendMessage, Answer.FILE_TOO_LARGE);
	}

	@Test
	void fileAlreadyValid() throws Exception {
		server.enqueue(MockResponses.FILE_ALREADY_VALID);
		server.enqueue(MockResponses.FILE_DOWNLOAD);
		server.enqueue(MockResponses.fileResponse(resources.image(512, 512, "png")));

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var getFile = server.takeRequest();
		assertEquals("/api/token/getFile", getFile.getPath());
		assertEquals("file_id=image.png", getFile.getBody().readUtf8());

		var download = server.takeRequest();
		assertEquals("/files/token/image/image.png", download.getPath());

		var sendMessage = server.takeRequest();
		assertEquals("/api/token/sendMessage", sendMessage.getPath());
		assertResponseContainsMessage(sendMessage, Answer.FILE_ALREADY_VALID);
	}

	@Test
	void fileConverted() throws Exception {
		server.enqueue(MockResponses.FILE_UPDATE);
		server.enqueue(MockResponses.FILE_DOWNLOAD);
		server.enqueue(MockResponses.fileResponse(resources.image(800, 400, "png")));

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var getFile = server.takeRequest();
		assertEquals("/api/token/getFile", getFile.getPath());
		assertEquals("file_id=image.png", getFile.getBody().readUtf8());

		var download = server.takeRequest();
		assertEquals("/files/token/image/image.png", download.getPath());

		var sendDocument = server.takeRequest();
		assertEquals("/api/token/sendDocument", sendDocument.getPath());
		assertThat(sendDocument.getBody().readUtf8(), containsString(Answer.FILE_READY.getText()));
	}

}
