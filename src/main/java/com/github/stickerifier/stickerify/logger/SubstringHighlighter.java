package com.github.stickerifier.stickerify.logger;

import static ch.qos.logback.core.pattern.color.ANSIConstants.BOLD;
import static ch.qos.logback.core.pattern.color.ANSIConstants.DEFAULT_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.ESC_END;
import static ch.qos.logback.core.pattern.color.ANSIConstants.ESC_START;
import static ch.qos.logback.core.pattern.color.ANSIConstants.GREEN_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.YELLOW_FG;
import static com.github.stickerifier.stickerify.telegram.model.TelegramRequest.NEW_USER;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.Converter;

import java.util.regex.Pattern;

/**
 * Custom converter class to be used by Logback in order to highlight important substrings.
 *
 * @see Converter
 */
public class SubstringHighlighter extends MessageConverter {

	private static final String START_YELLOW = changeColorTo(BOLD + YELLOW_FG);
	private static final String RESET_TEXT_ATTRIBUTES = "0;";
	static final String RESET_COLOR = changeColorTo(RESET_TEXT_ATTRIBUTES + DEFAULT_FG);
	static final String HIGHLIGHTED_NEW_USER = " " + START_YELLOW + NEW_USER.substring(1) + RESET_COLOR;
	static final String START_GREEN = changeColorTo(BOLD + GREEN_FG);
	private static final Pattern MIME_TYPE_PATTERN = Pattern.compile("[\\w-]+/[\\w-]+");

	@Override
	public String convert(ILoggingEvent event) {
		String message = event.getFormattedMessage();

		if (message != null) {
			if (message.contains(NEW_USER)) {
				return message.replaceFirst(Pattern.quote(NEW_USER), HIGHLIGHTED_NEW_USER);
			}

			var mimeType = getMimeType(message);
			if (mimeType != null) {
				var highlightedMimeType = START_GREEN + mimeType + RESET_COLOR;
				return message.replaceFirst(mimeType, highlightedMimeType);
			}
		}

		return message;
	}

	private static String changeColorTo(final String color) {
		return ESC_START + color + ESC_END;
	}

	private static String getMimeType(final String message) {
		var matcher = MIME_TYPE_PATTERN.matcher(message);

		return matcher.find() ? matcher.group() : null;
	}
}
