package com.github.stickerifier.stickerify.bot;

import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;
import okio.Okio;

import java.io.File;

public final class MockResponses {

	public static final MockResponse START_MESSAGE = new MockResponse().setBody("""
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
			""");

	public static final MockResponse HELP_MESSAGE = new MockResponse().setBody("""
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
			""");

	public static final MockResponse FILE_NOT_SUPPORTED = new MockResponse().setBody("""
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
			""");

	public static final MockResponse FILE_TOO_BIG = new MockResponse().setBody("""
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
			""");

	public static final MockResponse FILE_ALREADY_VALID = new MockResponse().setBody("""
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
			""");

	public static final MockResponse FILE_UPDATE = new MockResponse().setBody("""
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
			""");

	public static final MockResponse FILE_DOWNLOAD = new MockResponse().setBody("""
			{
				ok: true,
				result: {
					file_id: "image.png",
					file_path: "image/image.png"
				}
			}
			""");

	public static MockResponse fileResponse(File file) throws Exception {
		try (var buffer = new Buffer(); var source = Okio.source(file)) {
			buffer.writeAll(source);
			return new MockResponse().setBody(buffer);
		}
	}

	private MockResponses() {
		throw new UnsupportedOperationException();
	}
}
