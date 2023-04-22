package com.github.stickerifier.stickerify.logger;

import static com.github.stickerifier.stickerify.logger.NewUserHighlighter.HIGHLIGHTED_NEW_USER;
import static com.github.stickerifier.stickerify.telegram.model.TelegramRequest.NEW_USER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NewUserHighlighterTest {

	private static final String LOG_MESSAGE = "Received request";

	private NewUserHighlighter newUserHighlighter;

	@BeforeEach
	void setup() {
		newUserHighlighter = new NewUserHighlighter();
	}

	@Test
	@DisplayName("Log message from old user")
	void processEventWithOldUser() {
		var event = new LoggingEvent(LOG_MESSAGE);

		var convertedMessage = newUserHighlighter.convert(event);

		assertThat(convertedMessage, is(equalTo(LOG_MESSAGE)));
	}

	@Test
	@DisplayName("Log message from new user")
	void processEventWithNewUser() {
		var event = new LoggingEvent(LOG_MESSAGE + NEW_USER);

		var convertedMessage = newUserHighlighter.convert(event);

		assertThat(convertedMessage, is(equalTo(LOG_MESSAGE + HIGHLIGHTED_NEW_USER)));
	}

	@Test
	@DisplayName("Log message with multiple new user occurrences")
	void processEventWithMultipleNewUserOccurrences() {
		var event = new LoggingEvent(LOG_MESSAGE + NEW_USER + NEW_USER);

		var convertedMessage = newUserHighlighter.convert(event);

		assertThat(convertedMessage, is(equalTo(LOG_MESSAGE + HIGHLIGHTED_NEW_USER + NEW_USER)));
	}
}
