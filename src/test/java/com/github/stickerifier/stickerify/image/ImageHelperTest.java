package com.github.stickerifier.stickerify.image;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ImageHelperTest {

	@TempDir
	private File directory;

	private File result;

	@AfterEach
	void cleanup() throws IOException {
		if (result != null) {
			Files.deleteIfExists(result.toPath());
		}
	}

	@Test
	void resizeImage() throws Exception {
		var startingImage = image(1024, 1024, "jpg");
		result = ImageHelper.convertToPng(startingImage);

		assertImageConsistency(512, 512);
	}

	private File image(int width, int height, String extension) throws IOException {
		var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		var file = new File(directory, "%d x %d.%s".formatted(width, height, extension));
		ImageIO.write(image, extension, file);

		return file;
	}

	private void assertImageConsistency(int expectedWidth, int expectedHeight) throws IOException {
		var image = ImageIO.read(result);
		var actualExtension = result.getName().substring(result.getName().lastIndexOf('.'));

		assertAll(
				() -> assertEquals(".png", actualExtension),
				() -> assertEquals(expectedWidth, image.getWidth()),
				() -> assertEquals(expectedHeight, image.getHeight())
		);
	}

	@Test
	void resizeRectangularImage() throws Exception {
		var startingImage = image(1024, 512, "jpg");
		result = ImageHelper.convertToPng(startingImage);

		assertImageConsistency(512, 256);
	}

	@Test
	void resizeSmallImage() throws Exception {
		var startingImage = image(256, 256, "png");
		result = ImageHelper.convertToPng(startingImage);

		assertImageConsistency(512, 512);
	}

	@Test
	void resizeWebpImage() throws Exception {
		var startingImage = resource("valid.webp");
		result = ImageHelper.convertToPng(startingImage);

		assertImageConsistency(256, 512);
	}

	private File resource(String filename) {
		var resource = getClass().getClassLoader().getResource(filename);
		assertNotNull(resource, "Test resource [%s] not found.".formatted(filename));

		return new File(resource.getFile());
	}

	@Test
	void notAnImage() {
		var document = resource("document.txt");
		TelegramApiException exception = assertThrows(TelegramApiException.class, () -> ImageHelper.convertToPng(document));
		assertEquals("Passed-in file is not supported", exception.getMessage());
	}

}
