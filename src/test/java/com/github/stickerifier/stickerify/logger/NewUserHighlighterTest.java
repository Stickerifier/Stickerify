package com.github.stickerifier.stickerify.logger;

import static com.github.stickerifier.stickerify.logger.NewUserHighlighter.HIGHLIGHTED_NEW_USER;
import static com.github.stickerifier.stickerify.logger.NewUserHighlighter.RESET_COLOR;
import static com.github.stickerifier.stickerify.logger.NewUserHighlighter.START_YELLOW;
import static com.github.stickerifier.stickerify.telegram.model.TelegramRequest.NEW_USER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NewUserHighlighterTest {

	private NewUserHighlighter newUserHighlighter;

	@BeforeEach
	void setup() {
		newUserHighlighter = new NewUserHighlighter();
	}

	@Test
	@DisplayName("Log message from old user")
	void processEventWithOldUser() {
		var message = "Received request";
		var event = new LoggingEvent(message);

		var convertedMessage = newUserHighlighter.convert(event);

		assertAll(
				() -> assertThat(convertedMessage, not(containsString(START_YELLOW))),
				() -> assertThat(convertedMessage, not(containsString(RESET_COLOR))),
				() -> assertThat(convertedMessage, is(equalTo(message)))
		);
	}

	@Test
	@DisplayName("Log message from new user")
	void processEventWithNewUser() {
		var message = "Received request " + NEW_USER;
		var event = new LoggingEvent(message);

		var convertedMessage = newUserHighlighter.convert(event);

		assertAll(
				() -> assertThat(convertedMessage, containsString(HIGHLIGHTED_NEW_USER)),
				() -> assertThat(convertedMessage, is(not(equalTo(message))))
		);
	}
}
