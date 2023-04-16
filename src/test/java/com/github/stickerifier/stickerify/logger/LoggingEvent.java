package com.github.stickerifier.stickerify.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Test double that serves as an implementation of {@link ILoggingEvent}.
 */
class LoggingEvent extends LoggingEventVO {

	private IThrowableProxy throwable;
	private String loggerName;
	private String formattedMessage;

	LoggingEvent(String loggerName, boolean isExceptionLog) {
		this.loggerName = loggerName;

		if (isExceptionLog) {
			this.throwable = new ThrowableProxyVO() {
				@Override
				public String getClassName() {
					return TelegramApiException.class.getName();
				}

				@Override
				public StackTraceElementProxy[] getStackTraceElementProxyArray() {
					return new StackTraceElementProxy[] {};
				}
			};
		}
	}

	LoggingEvent(String formattedMessage) {
		this.formattedMessage = formattedMessage;
	}

	@Override
	public IThrowableProxy getThrowableProxy() {
		return throwable;
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public String getFormattedMessage() {
		return formattedMessage;
	}
}
