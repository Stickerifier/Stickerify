package com.github.stickerifier.stickerify.media;

/**
 * Class containing the constraints for Telegram stickers as documented in the documentation:
 *
 * @see <a href="https://core.telegram.org/stickers#static-stickers-and-emoji">Static stickers' constraints</a>
 * @see <a href="https://core.telegram.org/stickers/webm-vp9-encoding">Animated stickers' constraints</a>
 */
public final class MediaConstraints {

	static final int MAX_SIZE = 512;
	static final float MAX_FRAMES = 30F;
	static final long MAX_DURATION_MILLIS = 3_000L;
	static final String VP9_CODEC = "vp9";
	static final String MATROSKA_FORMAT = "matroska";
	static final long MAX_FILE_SIZE = 256_000L;

	private MediaConstraints() {
		throw new UnsupportedOperationException();
	}
}
