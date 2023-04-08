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
		var event = new LoggingEvent(MediaHelper.class.getName(), false);

		var convertedMessage = stackTraceConverter.convert(event);

		assertThat(convertedMessage, is(emptyString()));
	}

	@Test
	@DisplayName("Log message with exception thrown outside project classes")
	void processEventWithExternalException() {
		var event = new LoggingEvent(DefaultBotSession.class.getName(), true);

		var convertedMessage = stackTraceConverter.convert(event);

		assertThat(convertedMessage, is(emptyString()));
	}

	@Test
	@DisplayName("Log message with exception thrown inside project classes")
	void processEventWithInternalException() {
		var event = new LoggingEvent(MediaHelper.class.getName(), true);

		var convertedMessage = stackTraceConverter.convert(event);

		assertThat(convertedMessage, is(not(emptyString())));
	}

	private static class LoggingEvent extends LoggingEventVO {
		private IThrowableProxy throwable;
		private final String loggerName;

		private LoggingEvent(String loggerName, boolean isExceptionLog) {
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

		@Override
		public IThrowableProxy getThrowableProxy() {
			return throwable;
		}

		@Override
		public String getLoggerName() {
			return loggerName;
		}
	}
}
