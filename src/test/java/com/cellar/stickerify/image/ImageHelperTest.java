package com.cellar.stickerify.image;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

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

    private File image(int width, int height, String extension) {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var file = new File(directory, "%dx%d.%s".formatted(width, height, extension));
        try {
            ImageIO.write(image, extension, file);
        } catch (IOException e) {
            fail("Couldn't create image for test", e);
        }
        return file;
    }

    private String extension(File file) {
        return file.getName().split("\\.")[1];
    }

    @Test
    void resizeImage() throws Exception {
        var startingImage = image(1024, 1024, "jpg");
        result = ImageHelper.convertToPng(startingImage);
        var image = ImageIO.read(result);
        assertAll(
                () -> assertEquals("png", extension(result)),
                () -> assertEquals(512, image.getWidth()),
                () -> assertEquals(512, image.getHeight())
        );
    }

    @Test
    void resizeRectangularImage() throws Exception {
        var startingImage = image(1024, 512, "jpg");
        result = ImageHelper.convertToPng(startingImage);
        var image = ImageIO.read(result);
        assertAll(
                () -> assertEquals("png", extension(result)),
                () -> assertEquals(512, image.getWidth()),
                () -> assertEquals(256, image.getHeight())
        );
    }

    @Test
    void resizeSmallImage() throws Exception {
        var startingImage = image(256, 256, "png");
        result = ImageHelper.convertToPng(startingImage);
        var image = ImageIO.read(result);
        assertAll(
                () -> assertEquals("png", extension(result)),
                () -> assertEquals(512, image.getWidth()),
                () -> assertEquals(512, image.getHeight())
        );
    }

    @Nested
    class UnsupportedTypes {

        private File resource(String filename) {
            var resource = getClass().getClassLoader().getResource(filename);
            assertNotNull(resource, () -> "Test resource [%s] not found.".formatted(filename));
            return new File(resource.getFile());
        }

        @Test
        void notAnImage() {
            var document = resource("document.txt");
            TelegramApiException exception = assertThrows(TelegramApiException.class, () -> ImageHelper.convertToPng(document));
            assertEquals("Passed-in file is not supported", exception.getMessage());
        }

        @Test
        void webp() {
            var startingImage = resource("not_supported.webp");
            TelegramApiException exception = assertThrows(TelegramApiException.class, () -> ImageHelper.convertToPng(startingImage));
            assertEquals("Passed-in file is not supported", exception.getMessage());
        }

    }

}
