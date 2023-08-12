package com.github.stickerifier.stickerify.logger;

import ch.qos.logback.classic.pattern.ThreadConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.Converter;

/**
 * Custom converter class to be used by Logback in order
 * to retrieve the {@code threadId} instead of the thread name.
 *
 * @see Converter
 */
public class ThreadIdConverter extends ThreadConverter {

    @Override
    public String convert(final ILoggingEvent e) {
        return String.valueOf(Thread.currentThread().threadId());
    }
}
