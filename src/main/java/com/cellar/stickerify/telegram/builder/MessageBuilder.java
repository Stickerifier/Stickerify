package com.cellar.stickerify.telegram.builder;

import com.cellar.stickerify.telegram.Answer;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.File;

/**
 * Interface used to populate a Telegram response fluently.
 *
 * @param <T> the type of the object the {@link #build()} method will return
 */
public interface MessageBuilder<T extends PartialBotApiMethod<?>> {

	MessageBuilder<T> withChatId(Long chatId);

	MessageBuilder<T> withAnswer(Answer answer);

	default MessageBuilder<T> withReplyToMessageId(Integer messageId) {
		return this;
	}

	default MessageBuilder<T> withDocument(File file) {
		throw new UnsupportedOperationException();
	}

	T build();
}
