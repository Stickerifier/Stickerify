package com.cellar.stickerify.telegram.builder;

import com.cellar.stickerify.telegram.Answer;
import com.cellar.stickerify.telegram.model.TelegramRequest;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Builder used to fluently populate a text-only Telegram response.
 */
public class TextMessageBuilder {

	private final SendMessage message;

	/**
	 * @see TextMessageBuilder
	 */
	public TextMessageBuilder() {
		this.message = new SendMessage();
	}

	public TextMessageBuilder replyTo(TelegramRequest request) {
		message.setChatId(request.getChatId());
		return this;
	}

	public TextMessageBuilder withMessage(Answer answer) {
		message.setText(answer.getText());
		message.setParseMode(ParseMode.MARKDOWNV2);
		message.setDisableWebPagePreview(answer.isDisableWebPreview());
		return this;
	}

	public SendMessage build() {
		return message;
	}
}
