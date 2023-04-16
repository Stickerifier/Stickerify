package com.github.stickerifier.stickerify.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.github.stickerifier.stickerify.media.MediaHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class StackTraceCleanerTest {

	private StackTraceCleaner stackTraceCleaner;

	@BeforeEach
	void setup() {
		stackTraceCleaner = new StackTraceCleaner();
	}

	@Test
	@DisplayName("Log message without any exception")
	void processEventWithoutException() {
		var event = new LoggingEvent(MediaHelper.class.getName(), false);

		var convertedMessage = stackTraceCleaner.convert(event);

		assertThat(convertedMessage, is(emptyString()));
	}

	@Test
	@DisplayName("Log message with exception thrown outside project classes")
	void processEventWithExternalException() {
		var event = new LoggingEvent(DefaultBotSession.class.getName(), true);

		var convertedMessage = stackTraceCleaner.convert(event);

		assertThat(convertedMessage, is(emptyString()));
	}

	@Test
	@DisplayName("Log message with exception thrown inside project classes")
	void processEventWithInternalException() {
		var event = new LoggingEvent(MediaHelper.class.getName(), true);

		var convertedMessage = stackTraceCleaner.convert(event);

		assertThat(convertedMessage, is(not(emptyString())));
	}
}
