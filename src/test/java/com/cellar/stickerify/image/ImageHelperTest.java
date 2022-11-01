package com.cellar.stickerify.image;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ImageHelperTest {

    @Test
    void resizeImage() throws Exception {
        var startingImage = resource("1024x1024.jpg");
        var resultingImage = ImageHelper.convertToPng(startingImage);
        var image = ImageIO.read(resultingImage);
        assertAll(
                () -> assertEquals(512, image.getWidth()),
                () -> assertEquals(512, image.getHeight())
        );
    }

    @Test
    void resizeRectangularImage() throws Exception {
        var startingImage = resource("1024x512.jpg");
        var resultingImage = ImageHelper.convertToPng(startingImage);
        var image = ImageIO.read(resultingImage);
        assertAll(
                () -> assertEquals(512, image.getWidth()),
                () -> assertEquals(256, image.getHeight())
        );
    }

    @Test
    void resizeSmallImage() throws Exception {
        var startingImage = resource("256x256.jpg");
        var resultingImage = ImageHelper.convertToPng(startingImage);
        var image = ImageIO.read(resultingImage);
        assertAll(
                () -> assertEquals(512, image.getWidth()),
                () -> assertEquals(512, image.getHeight())
        );
    }

    @Test
    void unsupportedFile() {
        TelegramApiException exception = assertThrows(TelegramApiException.class, () -> {
            var startingImage = resource("document.txt");
            ImageHelper.convertToPng(startingImage);
        });
        assertEquals("Passed-in file is not supported", exception.getMessage());
    }

    @Test
    void unsupportedImageType() {
        TelegramApiException exception = assertThrows(TelegramApiException.class, () -> {
            var startingImage = resource("not_supported.webp");
            ImageHelper.convertToPng(startingImage);
        });
        assertEquals("Passed-in file is not supported", exception.getMessage());
    }

    static File resource(String filename) {
        return new File(Objects.requireNonNull(ImageHelperTest.class.getClassLoader().getResource(filename)).getFile());
    }

}
