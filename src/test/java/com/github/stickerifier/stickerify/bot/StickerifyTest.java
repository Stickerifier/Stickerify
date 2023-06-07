package com.github.stickerifier.stickerify.bot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.stickerifier.stickerify.telegram.Answer;
import com.pengrad.telegrambot.TelegramBot;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import okio.Okio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class StickerifyTest {

	@TempDir
	private File directory;

	public final MockWebServer server = new MockWebServer();

	@Test
	void startMessage() throws Exception {
		server.enqueue(Responses.START_MESSAGE);

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var sendMessage = server.takeRequest();
		assertEquals("/api/token/sendMessage", sendMessage.getPath());
		assertResponseContainsMessage(sendMessage.getBody().readUtf8(), Answer.ABOUT);
	}

	private static void startBot(MockWebServer server) {
		var telegramBot = new TelegramBot.Builder("token")
				.apiUrl(server.url("api/").toString())
				.fileApiUrl(server.url("files/").toString())
				.updateListenerSleep(500)
				.build();
		new Stickerify(telegramBot);
	}

	private static void assertResponseContainsMessage(String body, Answer answer) {
		var message = URLEncoder.encode(answer.getText(), StandardCharsets.UTF_8).replace("+", "%20");
		assertThat(body, containsString(message));
	}

	@Test
	void helpMessage() throws Exception {
		server.enqueue(Responses.HELP_MESSAGE);

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var sendMessage = server.takeRequest();
		assertEquals("/api/token/sendMessage", sendMessage.getPath());
		assertResponseContainsMessage(sendMessage.getBody().readUtf8(), Answer.HELP);
	}

	@Test
	void fileNotSupported() throws Exception {
		server.enqueue(Responses.FILE_NOT_SUPPORTED);

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var sendMessage = server.takeRequest();
		assertEquals("/api/token/sendMessage", sendMessage.getPath());
		assertResponseContainsMessage(sendMessage.getBody().readUtf8(), Answer.ERROR);
	}

	@Test
	void fileTooBig() throws Exception {
		server.enqueue(Responses.FILE_TOO_BIG);

		startBot(server);

		var getUpdates = server.takeRequest();
		assertEquals("/api/token/getUpdates", getUpdates.getPath());

		var sendMessage = server.takeRequest();
		assertEquals("/api/token/sendMessage", sendMessage.getPath());
		assertResponseContainsMessage(sendMessage.getBody().readUtf8(), Answer.FILE_TOO_LARGE);
	}

	@Test
	void fileAlreadyValid() throws Exception {
		server.enqueue(Responses.FILE_ALREADY_VALID);
		server.enqueue(Responses.FILE_DOWNLOAD);
		try (var buffer = new Buffer(); var source = Okio.source(image(512, 512))) {
			buffer.writeAll(source);
			server.enqueue(new MockResponse().setBody(buffer));
		}

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
		assertResponseContainsMessage(sendMessage.getBody().readUtf8(), Answer.FILE_ALREADY_VALID);
	}

	private File image(int width, int height) throws IOException {
		var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		var file = new File(directory, "%d x %d.png".formatted(width, height));
		ImageIO.write(image, "png", file);

		return file;
	}

	@Test
	void fileConverted() throws Exception {
		server.enqueue(Responses.FILE_UPDATE);
		server.enqueue(Responses.FILE_DOWNLOAD);
		try (var buffer = new Buffer(); var source = Okio.source(image(800, 400))) {
			buffer.writeAll(source);
			server.enqueue(new MockResponse().setBody(buffer));
		}

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
