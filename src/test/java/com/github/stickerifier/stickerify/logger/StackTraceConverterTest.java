package com.github.stickerifier.stickerify.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEventVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import com.github.stickerifier.stickerify.media.MediaHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class StackTraceConverterTest {

	private StackTraceConverter stackTraceConverter;

	@BeforeEach
	void setup() {
		stackTraceConverter = new StackTraceConverter();
	}

	@Test
	@DisplayName("Log message without any exception")
	void processEventWithoutException() {
		var event = new LoggingEvent(null, MediaHelper.class.getName());

		var convertedMessage = stackTraceConverter.convert(event);

		assertThat(convertedMessage, is(emptyString()));
	}

	@Test
	@DisplayName("Log message with exception thrown outside project classes")
	void processEventWithExternalException() {
		var throwable = new LoggingThrowable();
		var event = new LoggingEvent(throwable, DefaultBotSession.class.getName());

		var convertedMessage = stackTraceConverter.convert(event);

		assertThat(convertedMessage, is(emptyString()));
	}

	@Test
	@DisplayName("Log message with exception thrown inside project classes")
	void processEventWithInternalException() {
		var throwable = new LoggingThrowable();
		var event = new LoggingEvent(throwable, MediaHelper.class.getName());

		var convertedMessage = stackTraceConverter.convert(event);

		assertThat(convertedMessage, is(not(emptyString())));
	}

	private static class LoggingEvent extends LoggingEventVO {
		private final IThrowableProxy throwable;
		private final String loggerName;

		LoggingEvent(IThrowableProxy throwable, String loggerName) {
			this.throwable = throwable;
			this.loggerName = loggerName;
		}

		@Override
		public IThrowableProxy getThrowableProxy() {
			return throwable;
		}

		@Override
		public String getLoggerName() {
			return loggerName;
		}
	}

	private static class LoggingThrowable extends ThrowableProxyVO {
		@Override
		public String getClassName() {
			return TelegramApiException.class.getName();
		}

		@Override
		public StackTraceElementProxy[] getStackTraceElementProxyArray() {
			return new StackTraceElementProxy[] {};
		}
	}
}
