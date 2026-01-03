package com.github.stickerifier.stickerify;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jspecify.annotations.NullMarked;

import java.io.File;

@NullMarked
public final class ResourceHelper {

	public static File loadResource(String filename) {
		var resource = ResourceHelper.class.getClassLoader().getResource(filename);
		assertNotNull(resource, "Test resource [%s] not found.".formatted(filename));

		return new File(resource.getFile());
	}

	private ResourceHelper() {
		throw new UnsupportedOperationException();
	}
}
