package com.github.stickerifier.stickerify.logger;

import com.github.stickerifier.stickerify.telegram.model.TelegramRequest.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

public record StructuredLogger(Logger logger) {

	public static final ScopedValue<RequestDetails> REQUEST_DETAILS = ScopedValue.newInstance();
	public static final ScopedValue<String> MIME_TYPE = ScopedValue.newInstance();

	private static final String MIME_TYPE_LOG_KEY = "mime_type";
	private static final String REQUEST_DETAILS_LOG_KEY = "request_details";
	public static final String EXCEPTION_MESSAGE_LOG_KEY = "exception_message";
	public static final String FILE_ID_LOG_KEY = "file_id";
	public static final String ORIGINAL_REQUEST_LOG_KEY = "original_request";
	public static final String FILE_PATH_LOG_KEY = "file_path";
	public static final String STICKER_LOG_KEY = "sticker";

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
			logBuilder = logBuilder.addKeyValue(REQUEST_DETAILS_LOG_KEY, REQUEST_DETAILS.get());
		}
		if (MIME_TYPE.isBound()) {
			logBuilder = logBuilder.addKeyValue(MIME_TYPE_LOG_KEY, MIME_TYPE.get());
		}

		return logBuilder;
	}
}
