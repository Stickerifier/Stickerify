package com.github.stickerifier.stickerify.logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
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
		var event = mock(ILoggingEvent.class);
		when(event.getThrowableProxy()).thenReturn(null);

		var convertedMessage = stackTraceConverter.convert(event);

		assertThat(convertedMessage, is(emptyString()));
	}

	@Test
	@DisplayName("Log message with exception thrown outside project classes")
	void processEventWithExternalException() {
		var event = mock(ILoggingEvent.class);
		var throwable = mock(IThrowableProxy.class);
		when(event.getThrowableProxy()).thenReturn(throwable);
		when(throwable.getClassName()).thenReturn(TelegramApiException.class.getName());
		when(event.getLoggerName()).thenReturn(DefaultBotSession.class.getName());

		var convertedMessage = stackTraceConverter.convert(event);

		assertThat(convertedMessage, is(emptyString()));
	}

	@Test
	@DisplayName("Log message with exception thrown inside project classes")
	void processEventWithInternalException() {
		var event = mock(ILoggingEvent.class);
		var throwable = mock(IThrowableProxy.class);
		when(event.getThrowableProxy()).thenReturn(throwable);
		when(throwable.getClassName()).thenReturn(TelegramApiException.class.getName());
		when(event.getLoggerName()).thenReturn(MediaHelper.class.getName());
		when(throwable.getStackTraceElementProxyArray()).thenReturn(new StackTraceElementProxy[] {});

		var convertedMessage = stackTraceConverter.convert(event);

		assertThat(convertedMessage, is(not(emptyString())));
	}
}
