package com.github.stickerifier.stickerify.logger;

import static ch.qos.logback.core.pattern.color.ANSIConstants.DEFAULT_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.ESC_END;
import static ch.qos.logback.core.pattern.color.ANSIConstants.ESC_START;
import static com.github.stickerifier.stickerify.telegram.model.TelegramRequest.NEW_USER;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.Converter;

/**
 * Custom converter class to be used by Logback in order to
 * highlight substrings of the message to be logged.
 *
 * @see Converter
 */
public class NewUserHighlighter extends MessageConverter {

	private static final String BRIGHT_GREEN_FG = "92";
	static final String START_GREEN = changeColorTo(BRIGHT_GREEN_FG);
	static final String RESET_COLOR = changeColorTo(DEFAULT_FG);

	@Override
	public String convert(ILoggingEvent event) {
		String message = event.getFormattedMessage();

		if (message != null && message.contains(NEW_USER)) {
			return highlightNewUser(message);
		}

		return message;
	}

	private static String highlightNewUser(String message) {
		final int index = message.indexOf(NEW_USER);
		return message.substring(0, index) + START_GREEN + NEW_USER + RESET_COLOR + message.substring(index + NEW_USER.length());
	}

	private static String changeColorTo(String color) {
		return ESC_START + color + ESC_END;
	}
}
