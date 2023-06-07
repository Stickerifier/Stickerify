package com.github.stickerifier.stickerify.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.stickerifier.stickerify.telegram.Answer;
import com.pengrad.telegrambot.TelegramBot;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import okio.Okio;
import org.junit.Rule;
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

	@Rule
	public MockWebServer server = new MockWebServer();

	@Test
	void startMessage() throws Exception {
		server.enqueue(new MockResponse().setBody("""
				{
					ok: true,
					result: [{
						update_id: 1,
						message: {
							message_id: 1,
							from: { username: "User" },
							chat: { id: 1 },
							text: "/start"
						}
					}]
				}
				"""));

		startBot(server);

		var request1 = server.takeRequest();
		assertEquals("/api/token/getUpdates", request1.getPath());

		var request2 = server.takeRequest();
		assertEquals("/api/token/sendMessage", request2.getPath());
		assertEquals(sendMessageResponse(Answer.ABOUT), request2.getBody().readUtf8());
	}

	@Test
	void helpMessage() throws Exception {
		server.enqueue(new MockResponse().setBody("""
				{
					ok: true,
					result: [{
						update_id: 1,
						message: {
							message_id: 1,
							from: { username: "User" },
							chat: { id: 1 },
							text: "/help"
						}
					}]
				}
				"""));

		startBot(server);

		var request1 = server.takeRequest();
		assertEquals("/api/token/getUpdates", request1.getPath());

		var request2 = server.takeRequest();
		assertEquals("/api/token/sendMessage", request2.getPath());
		assertEquals(sendMessageResponse(Answer.HELP), request2.getBody().readUtf8());
	}

	@Test
	void fileNotSupported() throws Exception {
		server.enqueue(new MockResponse().setBody("""
				{
					ok: true,
					result: [{
						update_id: 1,
						message: {
							message_id: 1,
							from: { username: "User" },
							chat: { id: 1 },
							audio: {
								file_id: "audio.mp3"
							}
						}
					}]
				}
				"""));

		startBot(server);

		var request1 = server.takeRequest();
		assertEquals("/api/token/getUpdates", request1.getPath());

		var request2 = server.takeRequest();
		assertEquals("/api/token/sendMessage", request2.getPath());
		assertEquals(sendMessageResponse(Answer.ERROR), request2.getBody().readUtf8());
	}

	@Test
	void fileTooBig() throws Exception {
		server.enqueue(new MockResponse().setBody("""
				{
					ok: true,
					result: [{
						update_id: 1,
						message: {
							message_id: 1,
							from: { username: "User" },
							chat: { id: 1 },
							video: {
								file_id: "video.mp4",
								file_size: 100000000
							}
						}
					}]
				}
				"""));

		startBot(server);

		var request1 = server.takeRequest();
		assertEquals("/api/token/getUpdates", request1.getPath());

		var request2 = server.takeRequest();
		assertEquals("/api/token/sendMessage", request2.getPath());
		assertEquals(sendMessageResponse(Answer.FILE_TOO_LARGE), request2.getBody().readUtf8());
	}

	@Test
	void fileAlreadyValid() throws Exception {
		server.enqueue(new MockResponse().setBody("""
				{
					ok: true,
					result: [{
						update_id: 1,
						message: {
							message_id: 1,
							from: { username: "User" },
							chat: { id: 1 },
							photo: [{
								file_id: "image.png",
								file_size: 200000
							}]
						}
					}]
				}
				"""));
		server.enqueue(new MockResponse().setBody("""
				{
					ok: true,
					result: {
						file_id: "image.png",
						file_path: "image/image.png"
					}
				}
				"""));
		try (var buffer = new Buffer(); var source = Okio.source(image(512, 512))) {
			buffer.writeAll(source);
			server.enqueue(new MockResponse().setBody(buffer));
		}

		startBot(server);

		var request1 = server.takeRequest();
		assertEquals("/api/token/getUpdates", request1.getPath());

		var request2 = server.takeRequest();
		assertEquals("/api/token/getFile", request2.getPath());
		assertEquals("file_id=image.png", request2.getBody().readUtf8());

		var request3 = server.takeRequest();
		assertEquals("/files/token/image/image.png", request3.getPath());

		var request4 = server.takeRequest();
		assertEquals("/api/token/sendMessage", request4.getPath());
		assertEquals(sendMessageResponse(Answer.FILE_ALREADY_VALID), request4.getBody().readUtf8());
	}

	@Test
	void convertedFile() throws Exception {
		server.enqueue(new MockResponse().setBody("""
				{
					ok: true,
					result: [{
						update_id: 1,
						message: {
							message_id: 1,
							from: { username: "User" },
							chat: { id: 1 },
							photo: [{
								file_id: "image.png",
								file_size: 200000
							}]
						}
					}]
				}
				"""));
		server.enqueue(new MockResponse().setBody("""
				{
					ok: true,
					result: {
						file_id: "image.png",
						file_path: "image/image.png"
					}
				}
				"""));
		try (var buffer = new Buffer(); var source = Okio.source(image(800, 400))) {
			buffer.writeAll(source);
			server.enqueue(new MockResponse().setBody(buffer));
		}

		startBot(server);

		var request1 = server.takeRequest();
		assertEquals("/api/token/getUpdates", request1.getPath());

		var request2 = server.takeRequest();
		assertEquals("/api/token/getFile", request2.getPath());
		assertEquals("file_id=image.png", request2.getBody().readUtf8());

		var request3 = server.takeRequest();
		assertEquals("/files/token/image/image.png", request3.getPath());

		var request4 = server.takeRequest();
		assertEquals("/api/token/sendDocument", request4.getPath());
		// TODO: parse and validate multipart body
	}

	private void startBot(MockWebServer server) {
		var apiUrl = server.url("api/").toString();
		var fileApiUrl = server.url("files/").toString();
		var telegramBot = new TelegramBot.Builder("token").apiUrl(apiUrl).fileApiUrl(fileApiUrl).updateListenerSleep(500).build();
		new Stickerify(telegramBot);
	}

	private String sendMessageResponse(final Answer answer) {
		var message = URLEncoder.encode(answer.getText(), StandardCharsets.UTF_8).replace("+", "%20");
		return "chat_id=1&text=%s&parse_mode=MarkdownV2&disable_web_page_preview=%b".formatted(message, answer.isDisableWebPreview());
	}

	private File image(int width, int height) throws IOException {
		var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		var file = new File(directory, "%d x %d.png".formatted(width, height));
		ImageIO.write(image, "png", file);

		return file;
	}

}
