package com.github.stickerifier.stickerify.logger;

import com.github.stickerifier.stickerify.telegram.model.TelegramRequest.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

public record StructuredLogger(Logger logger) {

	public static final ScopedValue<RequestDetails> REQUEST_DETAILS = ScopedValue.newInstance();
	public static final ScopedValue<String> MIME_TYPE = ScopedValue.newInstance();

	public StructuredLogger(Class<?> clazz) {
		this(LoggerFactory.getLogger(clazz));
	}

	/**
	 * Creates a {@link LoggingEventBuilder} at the specified level with request details and MIME type information, if set.
	 *
	 * @param level the level of the log
	 * @return the log builder with context information
	 */
	public LoggingEventBuilder at(Level level) {
		var logBuilder = logger.atLevel(level);

		if (REQUEST_DETAILS.isBound()) {
			logBuilder = logBuilder.addKeyValue("request_details", REQUEST_DETAILS.get());
		}
		if (MIME_TYPE.isBound()) {
			logBuilder = logBuilder.addKeyValue("mime_type", MIME_TYPE.get());
		}

		return logBuilder;
	}
}
