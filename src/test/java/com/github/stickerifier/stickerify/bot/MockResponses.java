package com.github.stickerifier.stickerify.bot;

import mockwebserver3.MockResponse;
import okio.Buffer;
import okio.Okio;

import java.io.File;

public final class MockResponses {

	static final MockResponse START_MESSAGE = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						text: "/start"
					}
				}]
			}
			""");

	static final MockResponse HELP_MESSAGE = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						text: "/help"
					}
				}]
			}
			""");

	static final MockResponse FILE_NOT_SUPPORTED = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						audio: {
							file_id: "audio.mp3"
						}
					}
				}]
			}
			""");

	static final MockResponse FILE_TOO_BIG = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						video: {
							file_id: "video.mp4",
							file_size: 100000000
						}
					}
				}]
			}
			""");

	static final MockResponse ANIMATED_STICKER = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						sticker: {
							file_id: "animated_sticker.tgs",
							file_size: 64000
						}
					}
				}]
			}
			""");

	static final MockResponse PNG_FILE = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						photo: [{
							file_id: "image.png",
							file_size: 200000
						}]
					}
				}]
			}
			""");

	static final MockResponse WEBP_FILE = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						photo: [{
							file_id: "valid.webp",
							file_size: 200000
						}]
					}
				}]
			}
			""");

	static final MockResponse MOV_FILE = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						video: {
							file_id: "long.mov",
							file_size: 200000
						}
					}
				}]
			}
			""");

	static final MockResponse WEBM_FILE = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						video: {
							file_id: "short_low_fps.webm",
							file_size: 200000
						}
					}
				}]
			}
			""");

	static final MockResponse GIF_FILE = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						sticker: {
							file_id: "valid.gif",
							file_size: 200000
						}
					}
				}]
			}
			""");

	static final MockResponse DOCUMENT = new MockResponse().setBody("""
			{
				ok: true,
				result: [{
					update_id: 1,
					message: {
						message_id: 1,
						from: {
							username: "User"
						},
						chat: {
							id: 1
						},
						document: {
							file_id: "document.txt",
							file_size: 200000
						}
					}
				}]
			}
			""");

	static MockResponse fileInfo(String id) {
		return new MockResponse().setBody("""
				{
					ok: true,
					result: {
						file_id: "%s",
						file_path: "%s"
					}
				}
				""".formatted(id, id));
	}

	static MockResponse fileDownload(File file) throws Exception {
		try (var buffer = new Buffer(); var source = Okio.source(file)) {
			buffer.writeAll(source);
			return new MockResponse().setBody(buffer);
		}
	}

	private MockResponses() {
		throw new UnsupportedOperationException();
	}
}
