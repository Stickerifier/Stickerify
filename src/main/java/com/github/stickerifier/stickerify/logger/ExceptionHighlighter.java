package com.github.stickerifier.stickerify.logger;

import static ch.qos.logback.core.pattern.color.ANSIConstants.RED_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.RESET;
import static com.github.stickerifier.stickerify.logger.HighlightHelper.changeColorTo;
import static com.github.stickerifier.stickerify.logger.HighlightHelper.greenHighlight;
import static com.github.stickerifier.stickerify.logger.HighlightHelper.retrieveMimeType;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.Converter;

/**
 * Custom converter class to be used by Logback in order to highlight important substrings in exception logs.
 *
 * @see Converter
 */
public class ExceptionHighlighter extends ThrowableProxyConverter {

	static final String CONTINUE_RED = changeColorTo(RESET + RED_FG);

	@Override
	public String convert(ILoggingEvent event) {
		var fullMessage = super.convert(event);
		var throwable = event.getThrowableProxy();

		if (throwable != null && throwable.getMessage() != null) {
			var exceptionMessage = throwable.getMessage();
			var mimeType = retrieveMimeType(exceptionMessage);

			if (mimeType != null) {
				var highlightedMessage = exceptionMessage.replace(mimeType, greenHighlight(mimeType, CONTINUE_RED));
				return fullMessage.replaceFirst(exceptionMessage, highlightedMessage);
			}
		}

		return fullMessage;
	}
}
