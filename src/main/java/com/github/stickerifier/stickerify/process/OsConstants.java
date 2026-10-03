package com.github.stickerifier.stickerify.process;

public final class OsConstants {
	private static final String OS = System.getenv().getOrDefault("os.name", "");
	private static final boolean IS_WINDOWS = OS.toLowerCase().contains("windows");

	public static final String NULL_FILE = IS_WINDOWS ? "NUL" : "/dev/null";

	private OsConstants() {
		throw new UnsupportedOperationException();
	}
}
