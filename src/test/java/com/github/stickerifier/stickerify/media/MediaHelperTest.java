package com.github.stickerifier.stickerify.media;

import static com.github.stickerifier.stickerify.media.MediaConstraints.MATROSKA_FORMAT;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_VIDEO_FILE_SIZE;
import static com.github.stickerifier.stickerify.media.MediaConstraints.VP9_CODEC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.stickerifier.stickerify.ResourceHelper;
import com.github.stickerifier.stickerify.telegram.exception.TelegramApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MediaHelperTest {

	@TempDir
	private File directory;

	private ResourceHelper resources;

	private File result;

	@BeforeEach
	void setup() {
		resources = new ResourceHelper(directory);
	}

	@AfterEach
	void cleanup() throws IOException {
		if (result != null) {
			Files.deleteIfExists(result.toPath());
		}
	}

	@Test
	void resizeImage() throws Exception {
		var startingImage = resources.createImage(1024, 1024, "jpg");
		result = MediaHelper.convert(startingImage);

		assertImageConsistency(512, 512);
	}

	private void assertImageConsistency(int expectedWidth, int expectedHeight) throws IOException {
		var image = ImageIO.read(result);
		var actualExtension = result.getName().substring(result.getName().lastIndexOf('.'));

		assertAll("Image validation failed",
				() -> assertThat("image's extension must be png", actualExtension, is(equalTo(".png"))),
				() -> assertThat("image's width is not correct", image.getWidth(), is(equalTo(expectedWidth))),
				() -> assertThat("image's height is not correct", image.getHeight(), is(equalTo(expectedHeight)))
		);
	}

	@Test
	void resizeRectangularImage() throws Exception {
		var startingImage = resources.createImage(1024, 512, "jpg");
		result = MediaHelper.convert(startingImage);

		assertImageConsistency(512, 256);
	}

	@Test
	void resizeSmallImage() throws Exception {
		var startingImage = resources.createImage(256, 256, "png");
		result = MediaHelper.convert(startingImage);

		assertImageConsistency(512, 512);
	}

	@Test
	void noImageConversionNeeded() throws Exception {
		var startingImage = resources.createImage(512, 256, "png");
		result = MediaHelper.convert(startingImage);

		assertThat(result, is(nullValue()));
	}

	@Test
	void resizeWebpImage() throws Exception {
		var startingImage = resources.loadResource("valid.webp");
		result = MediaHelper.convert(startingImage);

		assertImageConsistency(256, 512);
	}

	@Test
	void convertLongMovVideo() throws Exception {
		var startingVideo = resources.loadResource("long.mov");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(512, 288, 30F, 3_000L);
	}

	private void assertVideoConsistency(int expectedWidth, int expectedHeight, float expectedFrameRate, long expectedDuration) throws EncoderException {
		var mediaInfo = new MultimediaObject(result).getInfo();
		var videoInfo = mediaInfo.getVideo();
		var videoSize = videoInfo.getSize();

		var actualExtension = result.getName().substring(result.getName().lastIndexOf('.'));

		assertAll("Video validation failed",
				() -> assertThat("video's extension must be webm", actualExtension, is(equalTo(".webm"))),
				() -> assertThat("video's width is not correct", videoSize.getWidth(), is(equalTo(expectedWidth))),
				() -> assertThat("video's height is not correct", videoSize.getHeight(), is(equalTo(expectedHeight))),
				() -> assertThat("video's frame rate is not correct", videoInfo.getFrameRate(), is(equalTo(expectedFrameRate))),
				() -> assertThat("video must be encoded with the VP9 codec", videoInfo.getDecoder(), startsWith(VP9_CODEC)),
				() -> assertThat("video's duration is not correct", mediaInfo.getDuration(), is(equalTo(expectedDuration))),
				() -> assertThat("video's format must be matroska", mediaInfo.getFormat(), is(equalTo(MATROSKA_FORMAT))),
				() -> assertThat("video must have no audio stream", mediaInfo.getAudio(), is(nullValue())),
				() -> assertThat("video size should not exceed 256 KB", Files.size(result.toPath()), is(lessThanOrEqualTo(MAX_VIDEO_FILE_SIZE)))
		);
	}

	@Test
	void convertShortAndLowFpsVideo() throws Exception {
		var startingVideo = resources.loadResource("short_low_fps.webm");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(512, 288, 10F, 1_000L);
	}

	@Test
	void resizeSmallWebmVideo() throws Exception {
		var startingVideo = resources.loadResource("small_video_sticker.webm");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(512, 212, 30F, 2_000L);
	}

	@Test
	void convertVerticalWebmVideo() throws Exception {
		var startingVideo = resources.loadResource("vertical_video_sticker.webm");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(288, 512, 30F, 2_000L);
	}

	@Test
	void convertGifVideo() throws Exception {
		var startingVideo = resources.loadResource("valid.gif");
		result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(512, 274, 10F, 1_000L);
	}

	@Test
	void noVideoConversionNeeded() throws Exception {
		var startingVideo = resources.loadResource("no_conversion_needed.webm");
		result = MediaHelper.convert(startingVideo);

		assertThat(result, is(nullValue()));
	}

	@Test
	void noAnimatedStickerConversionNeeded() throws Exception {
		var animatedSticker = resources.loadResource("animated_sticker.gz");
		result = MediaHelper.convert(animatedSticker);

		assertThat(result, is(nullValue()));
	}

	@Test
	void unsupportedGzipArchive() {
		var archive = resources.loadResource("unsupported_archive.gz");
		TelegramApiException exception = assertThrows(TelegramApiException.class, () -> MediaHelper.convert(archive));

		assertThat(exception.getMessage(), is(equalTo("Passed-in file is not supported")));
	}

	@Test
	void unsupportedFile() {
		var document = resources.loadResource("document.txt");
		TelegramApiException exception = assertThrows(TelegramApiException.class, () -> MediaHelper.convert(document));

		assertThat(exception.getMessage(), is(equalTo("Passed-in file is not supported")));
	}
}
