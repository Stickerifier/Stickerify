package com.cellar.stickerify.telegram.builder;

import com.cellar.stickerify.telegram.Message;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.File;

/**
 * Interface used to populate a Telegram response fluently.
 *
 * @param <T> the type of the object the {@link #build()} method will return
 */
public interface MessageBuilder<T extends PartialBotApiMethod<?>> {

	MessageBuilder<T> withChatId(Long chatId);

	MessageBuilder<T> withTextMessage(Message textMessage);

	default MessageBuilder<T> withReplyToMessageId(Integer messageId) {
		throw new UnsupportedOperationException();
	}

	default MessageBuilder<T> withDocument(File file) {
		throw new UnsupportedOperationException();
	}

	T build();
}
