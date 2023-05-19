package com.github.stickerifier.stickerify.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEventVO;

/**
 * Test double that serves as an implementation of {@link ILoggingEvent}.
 */
class LoggingEvent extends LoggingEventVO {

	private final String formattedMessage;

	LoggingEvent(String formattedMessage) {
		this.formattedMessage = formattedMessage;
	}

	@Override
	public String getFormattedMessage() {
		return formattedMessage;
	}
}
