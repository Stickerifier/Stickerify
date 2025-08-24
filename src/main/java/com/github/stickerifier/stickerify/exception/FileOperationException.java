package com.github.stickerifier.stickerify.exception;

public class FileOperationException extends MediaException {
	public FileOperationException(Throwable cause) {
		super(cause);
	}

	public FileOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileOperationException(String message) {
		super(message);
	}
}
