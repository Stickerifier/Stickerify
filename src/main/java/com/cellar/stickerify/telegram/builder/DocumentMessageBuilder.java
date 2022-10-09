package com.cellar.stickerify.telegram.builder;

import com.cellar.stickerify.telegram.Answer;
import com.cellar.stickerify.telegram.model.TelegramRequest;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;

/**
 * Builder used to fluently populate a Telegram reply containing a file.
 */
public class DocumentMessageBuilder {

	private final SendDocument message;

	/**
	 * @see DocumentMessageBuilder
	 */
	public DocumentMessageBuilder() {
		this.message = new SendDocument();
	}

	public DocumentMessageBuilder replyTo(TelegramRequest request) {
		message.setChatId(request.getChatId());
		message.setReplyToMessageId(request.getMessageId());
		return this;
	}

	public DocumentMessageBuilder withMessage(Answer answer) {
		message.setCaption(answer.getText());
		message.setParseMode(ParseMode.MARKDOWNV2);
		return this;
	}

	public DocumentMessageBuilder withFile(File file) {
		message.setDocument(new InputFile(file));
		return this;
	}

	public SendDocument build() {
		return message;
	}
}
