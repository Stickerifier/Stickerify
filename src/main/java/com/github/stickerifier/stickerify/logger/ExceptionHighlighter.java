package com.github.stickerifier.stickerify.logger;

import static ch.qos.logback.core.pattern.color.ANSIConstants.RED_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.RESET;
import static com.github.stickerifier.stickerify.logger.HighlightHelper.changeColorTo;
import static com.github.stickerifier.stickerify.logger.HighlightHelper.retrieveMimeType;
import static com.github.stickerifier.stickerify.logger.HighlightHelper.greenHighlight;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ExceptionHighlighter extends ThrowableProxyConverter {

	static final String CONTINUE_RED = changeColorTo(RESET + RED_FG);

	@Override
	public String convert(ILoggingEvent event) {
		var fullMessage = super.convert(event);
		var throwable = event.getThrowableProxy();

		if (throwable != null) {
			var exceptionMessage = throwable.getMessage();
			var mimeType = retrieveMimeType(exceptionMessage);

			if (mimeType != null) {
				var highlightedMessage = exceptionMessage.replaceFirst(mimeType, greenHighlight(mimeType, CONTINUE_RED));
				return fullMessage.replaceFirst(exceptionMessage, highlightedMessage);
			}
		}

		return fullMessage;
	}
}
