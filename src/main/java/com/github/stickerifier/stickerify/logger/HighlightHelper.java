package com.github.stickerifier.stickerify.logger;

import static ch.qos.logback.core.pattern.color.ANSIConstants.BOLD;
import static ch.qos.logback.core.pattern.color.ANSIConstants.ESC_END;
import static ch.qos.logback.core.pattern.color.ANSIConstants.ESC_START;
import static ch.qos.logback.core.pattern.color.ANSIConstants.GREEN_FG;

import java.util.regex.Pattern;

public final class HighlightHelper {

	static final String START_GREEN = changeColorTo(BOLD + GREEN_FG);
	private static final Pattern MIME_TYPE_PATTERN = Pattern.compile(" (\\w+/[-+.\\w]+) ");

	static String changeColorTo(final String color) {
		return ESC_START + color + ESC_END;
	}

	static String greenHighlight(final String message, String previousColor) {
		return START_GREEN + message + previousColor;
	}

	static String retrieveMimeType(final String message) {
		var matcher = MIME_TYPE_PATTERN.matcher(message);

		return matcher.find() ? matcher.group(1) : null;
	}
}
