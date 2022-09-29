package com.cellar.stickerify.telegram.builder;

import com.cellar.stickerify.telegram.Message;
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
	public MessageBuilder<SendMessage> withTextMessage(Message textMessage) {
		message.setText(textMessage.getText());
		message.setParseMode(ParseMode.MARKDOWN);
		message.setDisableWebPagePreview(textMessage.isDisableWebPreview());
		return this;
	}

	@Override
	public SendMessage build() {
		return message;
	}
}
