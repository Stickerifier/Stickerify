package com.github.stickerifier.stickerify.logger;

import static com.github.stickerifier.stickerify.logger.ExceptionHighlighter.CONTINUE_RED;
import static com.github.stickerifier.stickerify.logger.HighlightHelper.START_GREEN;
import static com.github.stickerifier.stickerify.logger.LoggingEvent.EXCEPTION_CLASS;
import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("log")
class ExceptionHighlighterTest {

	private static final String LOG_MESSAGE = "Received request";
	private static final String EXCEPTION_MESSAGE = "The video could not be processed successfully";
	private static final String MIME_TYPE = "text/plain";

	private ExceptionHighlighter exceptionHighlighter;

	@BeforeEach
	void setup() {
		exceptionHighlighter = new ExceptionHighlighter();
	}

	@Test
	@DisplayName("Log message without any exception")
	void processEventWithoutException() {
		var event = new LoggingEvent(LOG_MESSAGE);

		var convertedMessage = exceptionHighlighter.convert(event);

		assertThat(convertedMessage, is(emptyString()));
	}

	@Test
	@DisplayName("Log exception message without MIME type")
	void processExceptionEventWithoutMimeType() {
		var event = new LoggingEvent(LOG_MESSAGE, EXCEPTION_MESSAGE);
		var expectedMessage = "%s: %s".formatted(EXCEPTION_CLASS, EXCEPTION_MESSAGE);

		var convertedMessage = getFirstLine(exceptionHighlighter.convert(event));

		assertThat(convertedMessage, is(equalTo(expectedMessage)));
	}

	private static String getFirstLine(String text) {
		return text.split(lineSeparator())[0];
	}

	@Test
	@DisplayName("Log exception message with MIME type")
	void processExceptionEventWithMimeType() {
		var messageFormat = "The file with %s MIME type is not supported";
		var event = new LoggingEvent(LOG_MESSAGE, messageFormat.formatted(MIME_TYPE));
		var highlightedMimeType = START_GREEN + MIME_TYPE + CONTINUE_RED;
		var expectedMessage = "%s: %s".formatted(EXCEPTION_CLASS, messageFormat.formatted(highlightedMimeType));

		var convertedMessage = getFirstLine(exceptionHighlighter.convert(event));

		assertThat(convertedMessage, is(equalTo(expectedMessage)));
	}
}
