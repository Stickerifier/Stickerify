package com.github.stickerifier.stickerify.process;

import static java.util.Objects.requireNonNullElse;

public final class OsConstants {
	private static final boolean IS_WINDOWS = requireNonNullElse(System.getProperty("os.name"), "").toLowerCase().contains("windows");

	public static final String NULL_FILE = IS_WINDOWS ? "NUL" : "/dev/null";

	private OsConstants() {
		throw new UnsupportedOperationException();
	}
}
