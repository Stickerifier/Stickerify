package com.github.stickerifier.stickerify.media;

import static com.github.stickerifier.stickerify.media.MediaConstraints.MATROSKA_FORMAT;
import static com.github.stickerifier.stickerify.media.MediaConstraints.VP9_CODEC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
	void convertLongMovVideo() throws Exception {
		var startingVideo = resource("long.mov");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(512, 288, 30F, 3_000L);
	}

	private void assertVideoConsistency(int expectedWidth, int expectedHeight, float expectedFrameRate, long expectedDuration) throws EncoderException {
		var mediaInfo = new MultimediaObject(result).getInfo();
		var videoInfo = mediaInfo.getVideo();
		var videoSize = videoInfo.getSize();

		var actualExtension = result.getName().substring(result.getName().lastIndexOf('.'));

		assertAll(
				() -> assertThat(actualExtension, is(equalTo(".webm"))),
				() -> assertThat(videoSize.getWidth(), is(equalTo(expectedWidth))),
				() -> assertThat(videoSize.getHeight(), is(equalTo(expectedHeight))),
				() -> assertThat(videoInfo.getFrameRate(), is(equalTo(expectedFrameRate))),
				() -> assertThat(videoInfo.getDecoder(), startsWith(VP9_CODEC)),
				() -> assertThat(mediaInfo.getDuration(), is(equalTo(expectedDuration))),
				() -> assertThat(mediaInfo.getFormat(), is(equalTo(MATROSKA_FORMAT))),
				() -> assertThat(mediaInfo.getAudio(), is(nullValue()))
		);
	}

	@Test
	void convertShortAndLowFpsVideo() throws Exception {
		var startingVideo = resource("short_low_fps.webm");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(512, 288, 10F, 1_000L);
	}

	@Test
	void resizeSmallWebmVideo() throws Exception {
		var startingVideo = resource("small_video_sticker.webm");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(512, 212, 30F, 2_000L);
	}

	@Test
	void convertVerticalWebmVideo() throws Exception {
		var startingVideo = resource("vertical_video_sticker.webm");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(288, 512, 30F, 2_000L);
	}

	@Test
	void noConversionNeeded() throws Exception {
		var startingVideo = resource("no_conversion_needed.webm");
		result = MediaHelper.convert(startingVideo);

		assertThat(result, is(nullValue()));
	}

	@Test
	void notAnImage() {
		var document = resource("document.txt");
		TelegramApiException exception = assertThrows(TelegramApiException.class, () -> MediaHelper.convert(document));

		assertThat(exception.getMessage(), is(equalTo("Passed-in file is not supported")));
	}
}
