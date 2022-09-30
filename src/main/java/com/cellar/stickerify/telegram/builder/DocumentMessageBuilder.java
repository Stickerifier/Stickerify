package com.cellar.stickerify.telegram.builder;

import com.cellar.stickerify.telegram.Answer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;

public class DocumentMessageBuilder implements MessageBuilder<SendDocument> {

	private final SendDocument message;

	public DocumentMessageBuilder() {
		this.message = new SendDocument();
	}

	@Override
	public MessageBuilder<SendDocument> withChatId(Long chatId) {
		message.setChatId(chatId);
		return this;
	}

	@Override
	public MessageBuilder<SendDocument> withAnswer(Answer answer) {
		message.setCaption(answer.getText());
		message.setParseMode(ParseMode.MARKDOWN);
		return this;
	}

	@Override
	public MessageBuilder<SendDocument> withReplyToMessageId(Integer messageId) {
		message.setReplyToMessageId(messageId);
		return this;
	}

	@Override
	public MessageBuilder<SendDocument> withDocument(File file) {
		message.setDocument(new InputFile(file));
		return this;
	}

	@Override
	public SendDocument build() {
		return message;
	}
}
