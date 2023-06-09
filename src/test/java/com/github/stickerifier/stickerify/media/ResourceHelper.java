package com.github.stickerifier.stickerify.media;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class ResourceHelper {

	private final File directory;

	public ResourceHelper(File directory) {
		this.directory = directory;
	}

	public File image(int width, int height, String extension) throws IOException {
		var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		var file = new File(directory, "%d x %d.%s".formatted(width, height, extension));
		ImageIO.write(image, extension, file);

		return file;
	}

	public File resource(String filename) {
		var resource = getClass().getClassLoader().getResource(filename);
		assumeTrue(resource != null, "Test resource [%s] not found.".formatted(filename));

		return new File(resource.getFile());
	}

}
