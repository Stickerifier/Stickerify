package com.github.stickerifier.stickerify.logger;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;

/**
 * Custom converter class to be used by Logback in order to clear the stack trace
 * of an exception raised in the package {@code org.telegram.telegrambots}.
 */
public class StackTraceConverter extends ThrowableProxyConverter {

	private static final String TELEGRAMBOTS_ROOT_PACKAGE = "org.telegram.telegrambots";

	@Override
	public String convert(ILoggingEvent event) {
		var throwable = event.getThrowableProxy();

		if (throwable == null || throwable.getClassName() != null && event.getLoggerName().startsWith(TELEGRAMBOTS_ROOT_PACKAGE)) {
			return CoreConstants.EMPTY_STRING;
		}

		return super.convert(event);
	}
}
