package com.github.stickerifier.stickerify.logger;

import org.jspecify.annotations.Nullable;

import static ch.qos.logback.core.pattern.color.ANSIConstants.BOLD;
import static ch.qos.logback.core.pattern.color.ANSIConstants.ESC_END;
import static ch.qos.logback.core.pattern.color.ANSIConstants.ESC_START;
import static ch.qos.logback.core.pattern.color.ANSIConstants.GREEN_FG;

import java.util.regex.Pattern;

public final class HighlightHelper {

	static final String START_GREEN = changeColorTo(BOLD + GREEN_FG);
	private static final Pattern MIME_TYPE_PATTERN = Pattern.compile("(^|\\s)(\\w+/[-+.\\w]+)(?=\\s|$)");

	static String changeColorTo(final String color) {
		return ESC_START + color + ESC_END;
	}

	/**
	 * Enriches the {@code message} string with ANSI color codes to highlight it in green.
	 * Then, the string continues with the color specified by {@code previousColor}.
	 *
	 * @param message the message to be highlighted
	 * @param previousColor the color to use after the highlighted text
	 * @return the highlighted text
	 */
	static String greenHighlight(final String message, String previousColor) {
		return START_GREEN + message + previousColor;
	}

	static @Nullable String retrieveMimeType(final String message) {
		var matcher = MIME_TYPE_PATTERN.matcher(message);

		return matcher.find() ? matcher.group(2) : null;
	}

	static String replaceFirst(String message, String textToReplace, String replacement) {
		return message.replaceFirst(Pattern.quote(textToReplace), replacement);
	}

	private HighlightHelper() {
		throw new UnsupportedOperationException();
	}
}
