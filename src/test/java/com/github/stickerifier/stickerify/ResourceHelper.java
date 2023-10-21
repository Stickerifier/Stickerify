package com.github.stickerifier.stickerify;

import static org.junit.jupiter.api.Assumptions.abort;
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

	public File createImage(int width, int height, String extension) {
		var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		var file = new File(directory, "%d x %d.%s".formatted(width, height, extension));

		try {
			ImageIO.write(image, extension, file);
		} catch (IOException e) {
			abort("Image could not be written to file [%s].".formatted(file.getName()));
		} finally {
			image.flush();
		}

		return file;
	}

	public File loadResource(String filename) {
		var resource = getClass().getClassLoader().getResource(filename);
		assumeTrue(resource != null, "Test resource [%s] not found.".formatted(filename));

		return new File(resource.getFile());
	}

}
