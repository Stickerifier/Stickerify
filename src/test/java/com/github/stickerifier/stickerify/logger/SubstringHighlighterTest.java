package com.github.stickerifier.stickerify.logger;

import static com.github.stickerifier.stickerify.logger.SubstringHighlighter.HIGHLIGHTED_NEW_USER;
import static com.github.stickerifier.stickerify.logger.SubstringHighlighter.CONTINUE_PREVIOUS_COLOR;
import static com.github.stickerifier.stickerify.logger.SubstringHighlighter.START_GREEN;
import static com.github.stickerifier.stickerify.telegram.model.TelegramRequest.NEW_USER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SubstringHighlighterTest {

	private static final String LOG_MESSAGE = "Received request";
	private static final String MIME_TYPE = "image/png";
	private static final String LOG_MESSAGE_WITH_MIME_TYPE = LOG_MESSAGE + " with " + MIME_TYPE + " MIME type";

	private SubstringHighlighter substringHighlighter;

	@BeforeEach
	void setup() {
		substringHighlighter = new SubstringHighlighter();
	}

	@Test
	@DisplayName("Log message from old user")
	void processEventWithOldUser() {
		var event = new LoggingEvent(LOG_MESSAGE);

		var convertedMessage = substringHighlighter.convert(event);

		assertThat(convertedMessage, is(equalTo(LOG_MESSAGE)));
	}

	@Test
	@DisplayName("Log message from new user")
	void processEventWithNewUser() {
		var event = new LoggingEvent(LOG_MESSAGE + NEW_USER);

		var convertedMessage = substringHighlighter.convert(event);

		assertThat(convertedMessage, is(equalTo(LOG_MESSAGE + HIGHLIGHTED_NEW_USER)));
	}

	@Test
	@DisplayName("Log message with multiple new user occurrences")
	void processEventWithMultipleNewUserOccurrences() {
		var event = new LoggingEvent(LOG_MESSAGE + NEW_USER + NEW_USER);

		var convertedMessage = substringHighlighter.convert(event);

		assertThat(convertedMessage, is(equalTo(LOG_MESSAGE + HIGHLIGHTED_NEW_USER + NEW_USER)));
	}

	@Test
	@DisplayName("Log message with MIME type")
	void processEventWithMimeType() {
		var event = new LoggingEvent(LOG_MESSAGE_WITH_MIME_TYPE);
		var highlightedMimeType = START_GREEN + MIME_TYPE + CONTINUE_PREVIOUS_COLOR;

		var convertedMessage = substringHighlighter.convert(event);

		assertThat(convertedMessage, is(equalTo(LOG_MESSAGE + " with " + highlightedMimeType + " MIME type")));
	}

	@Test
	@DisplayName("Log message with multiple MIME types")
	void processEventWithMultipleMimeTypes() {
		var event = new LoggingEvent(LOG_MESSAGE_WITH_MIME_TYPE + " and " + MIME_TYPE);
		var highlightedMimeType = START_GREEN + MIME_TYPE + CONTINUE_PREVIOUS_COLOR;

		var convertedMessage = substringHighlighter.convert(event);

		assertThat(convertedMessage, is(equalTo(LOG_MESSAGE + " with " + highlightedMimeType + " MIME type and " + MIME_TYPE)));
	}
}
