package com.github.stickerifier.stickerify.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

public record StructuredLogger(Logger logger) {

	public static final ScopedValue<Long> USER_ID = ScopedValue.newInstance();
	public static final ScopedValue<String> MIME_TYPE = ScopedValue.newInstance();

	public StructuredLogger(Class<?> clazz) {
		this(LoggerFactory.getLogger(clazz));
	}

	public LoggingEventBuilder at(Level level) {
		var logBuilder = logger.atLevel(level);

		if (USER_ID.isBound()) {
			logBuilder = logBuilder.addKeyValue("user_id", USER_ID.get());
		}
		if (MIME_TYPE.isBound()) {
			logBuilder = logBuilder.addKeyValue("mime_type", MIME_TYPE.get());
		}

		return logBuilder;
	}
}
