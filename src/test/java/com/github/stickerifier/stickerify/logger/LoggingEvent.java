package com.github.stickerifier.stickerify.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import com.github.stickerifier.stickerify.exception.TelegramApiException;

/**
 * Test double that serves as an implementation of {@link ILoggingEvent}.
 */
class LoggingEvent extends LoggingEventVO {

	static final String EXCEPTION_CLASS = TelegramApiException.class.getName();

	private final String formattedMessage;
	private IThrowableProxy throwableProxy;

	LoggingEvent(String formattedMessage) {
		this.formattedMessage = formattedMessage;
	}

	LoggingEvent(String formattedMessage, String exceptionMessage) {
		this.formattedMessage = formattedMessage;
		this.throwableProxy = new ThrowableProxy(exceptionMessage);
	}

	private static class ThrowableProxy extends ThrowableProxyVO {
		private final String message;

		ThrowableProxy(String message) {
			this.message = message;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public String getClassName() {
			return EXCEPTION_CLASS;
		}

		@Override
		public StackTraceElementProxy[] getStackTraceElementProxyArray() {
			return new StackTraceElementProxy[] {};
		}
	}

	@Override
	public String getFormattedMessage() {
		return formattedMessage;
	}

	@Override
	public IThrowableProxy getThrowableProxy() {
		return throwableProxy;
	}
}
