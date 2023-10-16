package com.github.stickerifier.stickerify.media;

import static com.github.stickerifier.stickerify.media.MediaConstraints.MATROSKA_FORMAT;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_VIDEO_FILE_SIZE;
import static com.github.stickerifier.stickerify.media.MediaConstraints.VP9_CODEC;
import static com.github.stickerifier.stickerify.media.MediaHelper.FFMPEG_LOCATOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.stickerifier.stickerify.ResourceHelper;
import com.github.stickerifier.stickerify.junit.ClearTempFiles;
import com.github.stickerifier.stickerify.telegram.exception.TelegramApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@ClearTempFiles
class MediaHelperTest {

	@TempDir
	private File directory;

	private ResourceHelper resources;

	@BeforeEach
	void setup() {
		resources = new ResourceHelper(directory);
	}

	@Test
	void resizeImage() throws Exception {
		var startingImage = resources.createImage(1024, 1024, "jpg");
		var result = MediaHelper.convert(startingImage);

		assertImageConsistency(result, 512, 512);
	}

	private static void assertImageConsistency(File result, int expectedWidth, int expectedHeight) throws IOException {
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
		var result = MediaHelper.convert(startingImage);

		assertImageConsistency(result, 512, 256);
	}

	@Test
	void resizeSmallImage() throws Exception {
		var startingImage = resources.createImage(256, 256, "png");
		var result = MediaHelper.convert(startingImage);

		assertImageConsistency(result, 512, 512);
	}

	@Test
	void noImageConversionNeeded() throws Exception {
		var startingImage = resources.createImage(512, 256, "png");
		var result = MediaHelper.convert(startingImage);

		assertThat(result, is(nullValue()));
	}

	@Test
	void resizeWebpImage() throws Exception {
		var startingImage = resources.loadResource("valid.webp");
		var result = MediaHelper.convert(startingImage);

		assertImageConsistency(result, 256, 512);
	}

	@Test
	void convertLongMovVideo() throws Exception {
		var startingVideo = resources.loadResource("long.mov");
		var result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(result, 512, 288, 29.97F, 3_000L);
	}

	private static void assertVideoConsistency(File result, int expectedWidth, int expectedHeight, float expectedFrameRate, long expectedDuration) throws EncoderException {
		var mediaInfo = new MultimediaObject(result, FFMPEG_LOCATOR).getInfo();
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
	void convertMp4WithAudio() throws Exception {
		var startingVideo = resources.loadResource("video_with_audio.mp4");
		var result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(result, 512, 288, 29.97F, 3_000L);
	}

	@Test
	void convertShortAndLowFpsVideo() throws Exception {
		var startingVideo = resources.loadResource("short_low_fps.webm");
		var result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(result, 512, 288, 10F, 1_000L);
	}

	@Test
	void resizeSmallWebmVideo() throws Exception {
		var startingVideo = resources.loadResource("small_video_sticker.webm");
		var result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(result, 512, 212, 30F, 2_000L);
	}

	@Test
	void convertVerticalWebmVideo() throws Exception {
		var startingVideo = resources.loadResource("vertical_video_sticker.webm");
		var result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(result, 288, 512, 30F, 2_000L);
	}

	@Test
	void convertGifVideo() throws Exception {
		var startingVideo = resources.loadResource("valid.gif");
		var result = MediaHelper.convert(startingVideo);

		assertVideoConsistency(result, 512, 274, 10F, 1_000L);
	}

	@Test
	void noVideoConversionNeeded() throws Exception {
		var startingVideo = resources.loadResource("no_conversion_needed.webm");
		var result = MediaHelper.convert(startingVideo);

		assertThat(result, is(nullValue()));
	}

	@Test
	void noAnimatedStickerConversionNeeded() throws Exception {
		var animatedSticker = resources.loadResource("animated_sticker.tgs");
		var result = MediaHelper.convert(animatedSticker);

		assertThat(result, is(nullValue()));
	}

	@Test
	void noLowFpsAnimatedStickerConversionNeeded() throws Exception {
		var animatedSticker = resources.loadResource("low_fps_animated_sticker.tgs");
		var result = MediaHelper.convert(animatedSticker);

		assertThat(result, is(nullValue()));
	}

	@Test
	void unsupportedGzipArchive() {
		var archive = resources.loadResource("unsupported_archive.gz");

		assertThrows(TelegramApiException.class, () -> MediaHelper.convert(archive));
	}

	@Test
	void unsupportedFile() {
		var document = resources.loadResource("document.txt");

		assertThrows(TelegramApiException.class, () -> MediaHelper.convert(document));
	}

	@Nested
	@DisplayName("Concurrently convert")
	@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
	class ConcurrencyTest {

		@Test
		@DisplayName("mov videos")
		void concurrentMovVideoConversions() {
			var startingVideo = resources.loadResource("long.mov");

			executeConcurrentConversions(startingVideo);
		}

		private static void executeConcurrentConversions(File inputFile) {
			final int concurrentRequests = 50;
			var failedConversions = new AtomicInteger(0);

			try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
				IntStream.range(0, concurrentRequests).forEach(i -> executor.execute(() -> {
					try {
						MediaHelper.convert(inputFile);
					} catch (TelegramApiException e) {
						failedConversions.incrementAndGet();
					}
				}));
			}

			int failures = failedConversions.get();
			var errorMessage = "Unable to process %d concurrent requests: %d conversions failed".formatted(concurrentRequests, failures);

			assertThat(errorMessage, failures, is(equalTo(0)));
		}

		@Test
		@DisplayName("mp4 videos")
		void concurrentMp4VideoConversions() {
			var startingVideo = resources.loadResource("video_with_audio.mp4");

			executeConcurrentConversions(startingVideo);
		}

		@Test
		@DisplayName("webm videos")
		void concurrentWebmVideoConversions() {
			var startingVideo = resources.loadResource("small_video_sticker.webm");

			executeConcurrentConversions(startingVideo);
		}

		@Test
		@DisplayName("gif videos")
		void concurrentGifVideoConversions() {
			var startingVideo = resources.loadResource("valid.gif");

			executeConcurrentConversions(startingVideo);
		}

		@Test
		@DisplayName("webp images")
		void concurrentWebpImageConversions() {
			var startingImage = resources.loadResource("valid.webp");

			executeConcurrentConversions(startingImage);
		}

		@Test
		@DisplayName("png images")
		void concurrentPngImageConversions() {
			var startingImage = resources.createImage(256, 256, "png");

			executeConcurrentConversions(startingImage);
		}
	}
}
