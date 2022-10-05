package com.cellar.stickerify.image;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageHelperTest {

    /*
     *  Variable to store our demo JPG image
     */
    @TempDir
    File test_image;

    /*
     *  Variable to store the generated PNG image
     */
    File result_image;


    public void setUpImage(int width, int height, String extension) {
        /*
         * Function to create a JPG image based on the dimensions provided
         *
         */
        try {
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            ImageIO.write(img, extension, test_image);
        } catch (IOException ioe) {
            System.err.println("error creating temporary test file in " + this.getClass().getSimpleName());
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        /*
         * This method deletes the PNG file generated after conversion.
         *
         */
        if (result_image != null) Files.deleteIfExists(result_image.toPath());
    }

    @Test
    void convertToPngTest_CheckSquare() throws TelegramApiException, IOException {
        /*
            Test Case: Given big squared image, the result should be a 512x512 image
         */

        //Setting up test_image with a resolution of 1080x1080
        setUpImage(1080, 1080, "jpg");


        result_image = ImageHelper.convertToPng(test_image);
        assertEquals("png", result_image.getName().split("\\.")[1]);

        // Creating BufferedImage from File to make our assertions
        BufferedImage image = ImageIO.read(result_image);

        // Asserting that a big square image must be 512x512
        assertEquals(512, image.getWidth());
        assertEquals(512, image.getHeight());

    }

    @Test
    void convertToPng_CheckRectangle() throws TelegramApiException, IOException {
        /*
            Test Case: Given a rectangular image with side1 > side2, the result should still have a
            side1 > side2 relationship
         */

        //Setting up test_image with a resolution of 1080x720
        setUpImage(1080, 720, "jpg");

        result_image = ImageHelper.convertToPng(test_image);
        assertEquals("png", result_image.getName().split("\\.")[1]);

        // Creating BufferedImage from File to make our assertions
        BufferedImage image = ImageIO.read(result_image);

        // Asserting that width > height for both images
        assert (image.getWidth() > image.getHeight());
    }

    @Test
    void convertToPng_CheckSmall() throws TelegramApiException, IOException {
        /*
            Test Case: Given a small image, the result should have its longer side of 512 pixels
         */
        //Setting up test_image with a resolution of 180x120
        setUpImage(180, 120, "jpg");

        result_image = ImageHelper.convertToPng(test_image);
        assertEquals("png", result_image.getName().split("\\.")[1]);

        // Creating BufferedImage from File to make our assertions
        BufferedImage image = ImageIO.read(result_image);

        // Asserting that width = 512 for smaller images
        assertEquals(512, image.getWidth());
    }

    @Test
    void convertToPng_CheckInvalidFile() throws TelegramApiException {
         /*
            Test Case: Given a small image, the result should have its longer side of 512 pixels
         */

        //Setting up test_image with a resolution of 1080x720 and invalid extension
        setUpImage(1080, 720, "invalid");

        // Asserting TelegramApiException
        assertThrows(TelegramApiException.class, () -> ImageHelper.convertToPng(test_image));
    }

    @Test
    void convertToPng_CheckUnsupported() {
        //Setting up test_image with a resolution of 1080x720 with webp format
        setUpImage(1080, 720, "webp");

        // Asserting TelegramApiException
        assertThrows(TelegramApiException.class, () -> ImageHelper.convertToPng(test_image));

        //Setting up test_image with a resolution of 1080x720 with gif format
        setUpImage(1080, 720, "gif");

        // Asserting TelegramApiException
        assertThrows(TelegramApiException.class, () -> ImageHelper.convertToPng(test_image));

        //Setting up test_image with a resolution of 1080x720 with gifv format
        setUpImage(1080, 720, "gifv");

        // Asserting TelegramApiException
        assertThrows(TelegramApiException.class, () -> ImageHelper.convertToPng(test_image));
    }
}

