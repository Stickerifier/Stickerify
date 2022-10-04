package com.cellar.stickerify.telegram.builder;

import com.cellar.stickerify.telegram.Answer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class TextMessageBuilder implements MessageBuilder<SendMessage> {

	private final SendMessage message;

	public TextMessageBuilder() {
		this.message = new SendMessage();
	}

	@Override
	public MessageBuilder<SendMessage> withChatId(Long chatId) {
		message.setChatId(chatId);
		return this;
	}

	@Override
	public MessageBuilder<SendMessage> withAnswer(Answer answer) {
		message.setText(answer.getText());
		message.setParseMode(ParseMode.MARKDOWNV2);
		message.setDisableWebPagePreview(answer.isDisableWebPreview());
		return this;
	}

	@Override
	public SendMessage build() {
		return message;
	}
}
