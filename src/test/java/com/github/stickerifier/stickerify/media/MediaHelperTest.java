package com.github.stickerifier.stickerify.media;

import static com.github.stickerifier.stickerify.media.MediaConstraints.MATROSKA_FORMAT;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_DURATION_MILLIS;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_FRAMES;
import static com.github.stickerifier.stickerify.media.MediaConstraints.VP9_CODEC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MediaHelperTest {

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
		result = MediaHelper.convert(startingImage);

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
				() -> assertThat(actualExtension, is(equalTo(".png"))),
				() -> assertThat(image.getWidth(), is(equalTo(expectedWidth))),
				() -> assertThat(image.getHeight(), is(equalTo(expectedHeight)))
		);
	}

	@Test
	void resizeRectangularImage() throws Exception {
		var startingImage = image(1024, 512, "jpg");
		result = MediaHelper.convert(startingImage);

		assertImageConsistency(512, 256);
	}

	@Test
	void resizeSmallImage() throws Exception {
		var startingImage = image(256, 256, "png");
		result = MediaHelper.convert(startingImage);

		assertImageConsistency(512, 512);
	}

	@Test
	void resizeWebpImage() throws Exception {
		var startingImage = resource("valid.webp");
		result = MediaHelper.convert(startingImage);

		assertImageConsistency(256, 512);
	}

	private File resource(String filename) {
		var resource = getClass().getClassLoader().getResource(filename);
		assumeTrue(resource != null, "Test resource [%s] not found.".formatted(filename));

		return new File(resource.getFile());
	}

	@Test
	void convertMovVideo() throws Exception {
		var startingVideo = resource("valid.mov");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency();
	}

	private void assertVideoConsistency() throws EncoderException {
		var mediaInfo = new MultimediaObject(result).getInfo();
		var videoInfo = mediaInfo.getVideo();

		var actualExtension = result.getName().substring(result.getName().lastIndexOf('.'));

		assertAll(
				() -> assertThat(actualExtension, is(equalTo(".webm"))),
				() -> assertTrue(MediaHelper.isSizeCompliant(videoInfo.getSize())),
				() -> assertThat(videoInfo.getFrameRate(), is(lessThanOrEqualTo(MAX_FRAMES))),
				() -> assertThat(videoInfo.getDecoder(), startsWith(VP9_CODEC)),
				() -> assertThat(mediaInfo.getDuration(), is(lessThanOrEqualTo(MAX_DURATION_MILLIS))),
				() -> assertThat(mediaInfo.getFormat(), is(equalTo(MATROSKA_FORMAT))),
				() -> assertThat(mediaInfo.getAudio(), is(nullValue()))
		);
	}

	@Test
	void convertWebmVideo() throws Exception {
		var startingVideo = resource("valid.webm");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency();
	}

	@Test
	void resizeSmallWebmVideo() throws Exception {
		var startingVideo = resource("small_animated_sticker.webm");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency();
	}

	@Test
	void notAnImage() {
		var document = resource("document.txt");
		TelegramApiException exception = assertThrows(TelegramApiException.class, () -> MediaHelper.convert(document));
		assertThat(exception.getMessage(), is(equalTo("Passed-in file is not supported")));
	}

}
